import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

class Cliente {
    int clienteId;
    float saldo;

    public Cliente(int clienteId, float saldo) {
        this.clienteId = clienteId;
        this.saldo = saldo;
    }

    public void descontarSaldo(float valor) {
        this.saldo -= valor;
    }

    public float getSaldo() {
        return saldo;
    }
}

class ClienteDestino {
    String nomeInstituicao;
    int numeroConta;

    public ClienteDestino(String nomeInstituicao, int numeroConta) {
        this.nomeInstituicao = nomeInstituicao;
        this.numeroConta = numeroConta;
    }
}

public class Banco {

    private static final HashMap<Integer, Cliente> clientes = new HashMap<>();

    public static void main(String[] args) {
        // Inicialização de clientes (considerando 5000 clientes)
        for (int i = 1; i <= 5000; i++) {
            clientes.put(i, new Cliente(i, 1000.0f));
        }

        spark.Spark.port(8080);

        spark.Spark.get("/transferenciaExterna", (req, res) -> {
            try {
                int clienteId = Integer.parseInt(req.queryParams("clienteId"));
                float valorTransferencia = Float.parseFloat(req.queryParams("valor"));
                String chaveDestino = req.queryParams("chaveDestino");

                Cliente clienteOrigem = clientes.get(clienteId);

                if (clienteOrigem != null && clienteOrigem.getSaldo() >= valorTransferencia) {
                    ClienteDestino clienteDestinoPix = obterClienteDestino(chaveDestino);

                    if (clienteDestinoPix != null) {
                        clienteOrigem.descontarSaldo(valorTransferencia);
                        System.out.println("Transferência Externa: " + valorTransferencia + " para cliente " +
                                clienteDestinoPix.numeroConta + " da instituição " + clienteDestinoPix.nomeInstituicao);
                        return respostaSucesso("Transferência externa realizada com sucesso.");
                    } else {
                        return respostaErro("Chave destino inválida.");
                    }
                } else {
                    return respostaErro("Saldo insuficiente ou cliente não encontrado.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return respostaErro("Erro ao processar a solicitação.");
            }
        });
    }

    private static ClienteDestino obterClienteDestino(String chaveDestino) {
        try {
            String apiUrl = "http://localhost:8080/transferenciaExterna?chaveDestino=" + chaveDestino;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
    
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
    
                JsonParser parser = new JsonParser();
                JsonObject jsonResponse = parser.parse(response.toString()).getAsJsonObject();
    
                String instituicaoNome = jsonResponse.getAsJsonObject("clienteData").get("nomeInstituicao").getAsString();
                int clienteDestinoId = jsonResponse.getAsJsonObject("clienteData").get("numeroConta").getAsInt();
    
                return new ClienteDestino(instituicaoNome, clienteDestinoId);
            } else {
                System.out.println("Falha na chamada à API externa: " + responseCode);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String respostaSucesso(String mensagem) {
        Gson gson = new Gson();
        JsonObject resposta = new JsonObject();
        resposta.addProperty("status", "success");
        resposta.addProperty("mensagem", mensagem);
        return gson.toJson(resposta);
    }

    private static String respostaErro(String mensagem) {
        Gson gson = new Gson();
        JsonObject resposta = new JsonObject();
        resposta.addProperty("status", "error");
        resposta.addProperty("mensagem", mensagem);
        return gson.toJson(resposta);
    }
}