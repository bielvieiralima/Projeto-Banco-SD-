#include <iostream>
#include <unordered_map>
#include <cpp_httplib.h>
#include <vector>
#include <thread>

struct TransferenciaExternaRequest {
    int clienteId;
    float valor;
    std::string chaveDestino;
};

struct TransferenciaExternaResponse {
    std::string status;
    std::string mensagem;
};

class InstituicaoFinanceira {
public:
    int porta;
    std::string nome;

    InstituicaoFinanceira(int porta, const std::string& nome) : porta(porta), nome(nome) {}

    TransferenciaExternaResponse fazerTransferencia(const TransferenciaExternaRequest& request) {
        // Lógica para processar a transferência na instituição financeira
        // Implemente conforme necessário para interagir com o sistema da instituição financeira
        return TransferenciaExternaResponse{"success", "Transferência interna realizada com sucesso na " + nome};
    }
};

class BancoCentral {
private:
    static std::unordered_map<std::string, InstituicaoFinanceira> instituicoes;

public:
    static void iniciarServico() {
        instituicoes["A"] = InstituicaoFinanceira(8081, "InstituicaoA");
        instituicoes["B"] = InstituicaoFinanceira(8082, "InstituicaoB");
        instituicoes["C"] = InstituicaoFinanceira(8083, "InstituicaoC");

        httplib::Server servidor;

        servidor.Post("/transferenciaExterna", [](const httplib::Request& req, httplib::Response& res) {
            TransferenciaExternaRequest transferenciaRequest;
            from_json(httplib::JsonValue(req.body), transferenciaRequest);

            std::string instituicaoDestino = transferenciaRequest.chaveDestino.substr(0, 1);
            auto it = instituicoes.find(instituicaoDestino);

            if (it != instituicoes.end()) {
                TransferenciaExternaResponse response = it->second.fazerTransferencia(transferenciaRequest);
                res.set_content(to_json(response).dump(), "application/json");
            } else {
                res.set_content(respostaErro("Instituição destino não encontrada."), "application/json");
            }
        });

        servidor.listen("localhost", 8080);
    }

private:
    static std::string respostaErro(const std::string& mensagem) {
        TransferenciaExternaResponse response{"error", mensagem};
        return to_json(response).dump();
    }
};

std::unordered_map<std::string, InstituicaoFinanceira> BancoCentral::instituicoes;

int main() {
    BancoCentral::iniciarServico();

    // Criar threads para simular processos de instituições financeiras
    std::vector<std::thread> threads;
    for (auto& [chave, instituicao] : BancoCentral::instituicoes) {
        threads.emplace_back([&instituicao]() {
            // Lógica de execução da instituição financeira
            // Pode incluir processos específicos da instituição financeira
        });
    }

    // Aguardar até que todas as threads terminem
    for (auto& thread : threads) {
        thread.join();
    }

    return 0;
}