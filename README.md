# Detector Estático de Padrões de Design

GitHub: https://github.com/hitalloazevedo/design-pattern-detector

A ferramenta realiza análise estática da estrutura do código-fonte utilizando a Árvore Sintática Abstrata (AST) e resolução de símbolos, sem executar o programa. A detecção é baseada apenas na estrutura das classes e em seus relacionamentos.

---

## Padrões Suportados

### Decorator

#### (a) Quais elementos estruturais do código você decidiu observar e por quê?

Para identificar o padrão Decorator, a ferramenta verifica se uma classe implementa uma interface (ou estende uma classe abstrata) e, ao mesmo tempo, possui um atributo desse mesmo tipo. Também é verificado se esse objeto é armazenado pela classe (por meio do construtor ou de um setter) e se os métodos delegam chamadas para ele.

Esses elementos foram escolhidos porque representam a principal característica estrutural do Decorator: uma classe encapsula outro objeto que possui o mesmo contrato e adiciona comportamento antes ou depois da delegação.

#### (b) Quais combinações de elementos configuram o padrão?

O padrão é identificado quando são encontrados os seguintes elementos:

- uma interface ou classe abstrata;
- uma classe que implementa ou estende essa abstração;
- um atributo do mesmo tipo dentro dessa classe;
- atribuição desse atributo por construtor ou setter;
- pelo menos um método que delega a execução para o objeto armazenado.

Exemplo estrutural:

```java
interface A {
    void executar();
}

class B implements A {

    private final A elemento;

    B(A elemento) {
        this.elemento = elemento;
    }

    @Override
    public void executar() {
        // comportamento adicional
        elemento.executar();
        // comportamento adicional
    }
}
```

---

### Adapter

#### (a) Quais elementos estruturais do código você decidiu observar e por quê?

Para detectar o padrão Adapter, a ferramenta procura uma classe que implementa um determinado contrato, mas mantém internamente um objeto de outro tipo. Também é verificado se esse objeto é armazenado pela classe e se os métodos implementados delegam chamadas para ele.

Essa estrutura diferencia o Adapter do Decorator, pois o objeto encapsulado possui um tipo diferente da abstração implementada pela classe.

#### (b) Quais combinações de elementos configuram o padrão?

O padrão é identificado quando são encontrados:

- uma interface ou classe abstrata;
- uma classe que implementa essa abstração;
- um atributo de outro tipo;
- atribuição desse atributo por construtor ou setter;
- pelo menos um método que delega chamadas para esse objeto.

Exemplo estrutural:

```java
interface A {
    String executar(int valor);
}

class B {

    double processar(String valor) {
        return 0;
    }

}

class C implements A {

    private final B elemento;

    C(B elemento) {
        this.elemento = elemento;
    }

    @Override
    public String executar(int valor) {
        return String.valueOf(
                elemento.processar(
                        String.valueOf(valor)
                )
        );
    }

}
```

---

### Strategy

#### (a) Quais elementos estruturais do código você decidiu observar e por quê?

Para detectar o padrão Strategy, a ferramenta procura uma interface (ou classe abstrata) com duas ou mais implementações concretas. Em seguida, verifica se existe outra classe que mantém uma referência para essa abstração e delega parte do seu comportamento para esse objeto.

Esses elementos indicam que diferentes implementações podem ser utilizadas sem alterar a classe responsável pela execução.

#### (b) Quais combinações de elementos configuram o padrão?

O padrão é identificado quando são encontrados:

- uma interface ou classe abstrata;
- duas ou mais implementações dessa abstração;
- uma classe que possui um atributo desse tipo;
- atribuição desse atributo por construtor ou setter;
- pelo menos um método que delega a execução para o objeto armazenado.

Exemplo estrutural:

```java
interface A {
    int executar(int a, int b);
}

class B implements A {

    @Override
    public int executar(int a, int b) {
        return a + b;
    }

}

class C implements A {

    @Override
    public int executar(int a, int b) {
        return a * b;
    }

}

class D {

    private A estrategia;

    D(A estrategia) {
        this.estrategia = estrategia;
    }

    void alterar(A estrategia) {
        this.estrategia = estrategia;
    }

    int processar(int a, int b) {
        return estrategia.executar(a, b);
    }

}
```

---

## Limitações

A ferramenta utiliza apenas características estruturais do código. Por isso, podem ocorrer falsos positivos quando uma estrutura semelhante é utilizada sem a intenção de implementar um padrão de projeto.

Também podem ocorrer falsos negativos quando a implementação utiliza estruturas diferentes das normalmente empregadas, principalmente quando não há delegação explícita entre objetos. No caso do Strategy, por exemplo, a detecção depende da existência de duas ou mais implementações da mesma abstração.

Além disso, o detector não analisa o comportamento do programa em tempo de execução. Implementações baseadas em reflexão, geração dinâmica de objetos, proxies, lambdas ou mecanismos externos de geração e injeção de objetos podem não ser identificadas corretamente.