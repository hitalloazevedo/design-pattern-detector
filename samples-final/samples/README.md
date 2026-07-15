# Exemplos para o relatório final

Execute cada exemplo separadamente para obter uma saída limpa:

```bash
mvn exec:java -Dexec.args="samples/generic"
mvn exec:java -Dexec.args="samples/adapter"
mvn exec:java -Dexec.args="samples/strategy"
```

- `generic`: Decorator com nomes genéricos (`A`, `B`, `C`).
- `adapter`: Adapter com nomes descritivos.
- `strategy`: Strategy com duas implementações concretas e configuração por setter.

A pasta foi limpa para não incluir arquivos de depuração nem o exemplo `validation`, que satisfazia a heurística simples de Adapter e gerava um falso positivo esperado.
