import csv
import math
import statistics
from collections import defaultdict

def analizar_resultados(ruta_csv="mediciones_oltp_600.csv"):
    grupos = defaultdict(list)
    with open(ruta_csv, mode="r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            vol = int(row["volumen_registros"])
            op = row["operacion_oltp"]
            lat = float(row["latencia_ms"])
            grupos[(op, vol)].append(lat)

    operaciones = ["SELECT", "INSERT", "UPDATE", "DELETE"]
    volumenes = [100, 1000, 10000, 50000, 100000]

    print("OPERACION | VOLUMEN | MEDIA(ms) | MEDIANA(ms) | MIN(ms) | MAX(ms) | STD_DEV | P95(ms) | VAR_VS_100(%)")
    print("-" * 98)

    for op in operaciones:
        media_base = None
        for vol in volumenes:
            datos = sorted(grupos[(op, vol)])
            if not datos:
                continue
            media = statistics.mean(datos)
            mediana = statistics.median(datos)
            minimo = min(datos)
            maximo = max(datos)
            std_dev = statistics.stdev(datos) if len(datos) > 1 else 0.0
            p95 = datos[math.ceil(0.95 * len(datos)) - 1]

            if media_base is None:
                media_base = media
                var_pct = 0.0
            else:
                var_pct = ((media - media_base) / media_base) * 100.0

            print(f"{op:<9} | {vol:>7} | {media:>9.4f} | {mediana:>11.4f} | {minimo:>7.4f} | {maximo:>7.4f} | {std_dev:>7.4f} | {p95:>7.4f} | {var_pct:>12.2f}%")

if __name__ == "__main__":
    analizar_resultados()
