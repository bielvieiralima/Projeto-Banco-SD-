import requests
import random
import string
import time

# Função para gerar uma chave de destino
def gerar_chave_destino():
    tamanho_chave = 8
    caracteres = string.ascii_letters + string.digits
    return ''.join(random.choice(caracteres) for _ in range(tamanho_chave))

# Função para realizar transferência externa
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
            print(f"Transferência realizada (Cliente ID {cliente_id}): {response.json()}")
        else:
            print(f"Falha na transferência para o Cliente ID {cliente_id}, código de status: {response.status_code}")
    except Exception as e:
        print(f"Erro ao realizar a transferência: {str(e)}")

# Simula transferências para diferentes instituições
def simular_transferencias():
    for instituicao_id in range(1, 4):
        for cliente_id in range(1, 6):  # Reduzido para facilitar os testes
            valor_transferencia = round(random.uniform(0.0, 10.0), 2)
            chave_destino = gerar_chave_destino()

            realizar_transferencia(cliente_id, valor_transferencia, chave_destino)
            time.sleep(1)  # Aguarda 1 segundo entre transferências

if __name__ == "__main__":
    simular_transferencias()