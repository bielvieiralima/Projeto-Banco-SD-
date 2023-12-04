import requests
import random
import string

class Cliente:
    def __init__(self, cliente_id, saldo):
        self.cliente_id = cliente_id
        self.saldo = saldo

    def descontar_saldo(self, valor):
        self.saldo -= valor

class ClienteDestino:
    def __init__(self, nome_instituicao, numero_conta):
        self.nome_instituicao = nome_instituicao
        self.numero_conta = numero_conta

def realizar_transferencia(cliente_id, valor_transferencia, chave_destino):
    base_url = "http://localhost:8080/transferenciaExterna"
    try:
        query_params = {
            "clienteId": cliente_id,
            "valor": valor_transferencia,
            "chaveDestino": chave_destino
        }
        response = requests.get(base_url, params=query_params)

        if response.status_code == 200:
            cliente_destino_pix_data = response.json()["clienteData"]
            cliente_destino_pix = ClienteDestino(
                cliente_destino_pix_data["nomeInstituicao"],
                cliente_destino_pix_data["numeroConta"]
            )

            print(f"Transferência realizada (Cliente ID {cliente_id}): {cliente_destino_pix.__dict__}")
        else:
            print(f"Falha na transferência para o Cliente ID {cliente_id}, código de status: {response.status_code}")
    except Exception as e:
        print(f"Erro ao realizar a transferência: {str(e)}")

def gerar_chave_destino():
    tamanho_chave = 8  # Defina o tamanho desejado da chave
    caracteres = string.ascii_letters + string.digits  # Letras e números

    chave_destino = ''.join(random.choice(caracteres) for _ in range(tamanho_chave))
    return chave_destino

def main():
    random.seed()

    # Simulando transferências para diferentes instituições
    for instituicao_id in range(1, 4):
        for cliente_id in range(1, 5001):
            valor_transferencia = round(random.uniform(0.0, 10.0), 2)
            chave_destino = gerar_chave_destino()

            realizar_transferencia(cliente_id, valor_transferencia, chave_destino)

if __name__ == "__main__":
    main()