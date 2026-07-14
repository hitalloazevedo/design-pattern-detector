# Detector Estático de Padrões de Design

GitHub: https://github.com/hitalloazevedo/design-pattern-detector

## Padrões Suportados

### Decorator

#### (a) Quais elementos estruturais do código você decidiu observar e por quê?

Para identificar o padrão Decorator, a ferramenta verifica se uma classe implementa uma interface (ou estende uma classe abstrata) e, ao mesmo tempo, possui um atributo desse mesmo tipo. Também é verificado se esse objeto é recebido pelo construtor e se os métodos da classe delegam chamadas para ele.

Esses elementos foram escolhidos porque representam a principal característica estrutural do Decorator: a classe envolve outro objeto que possui o mesmo contrato e adiciona um comportamento antes ou depois da chamada original.

#### (b) Quais combinações de elementos configuram o padrão?

O padrão é identificado quando são encontrados os seguintes elementos:

- uma interface ou classe abstrata;
- uma classe que implementa ou estende esse tipo;
- um atributo do mesmo tipo dentro dessa classe;
- inicialização desse atributo por construtor (ou outro mecanismo equivalente);
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

Para detectar o padrão Adapter, a ferramenta procura uma classe que implementa um determinado contrato, mas mantém internamente um objeto de outro tipo. Além disso, verifica se os métodos implementados encaminham a execução para esse objeto, realizando adaptações quando necessário.

Essa estrutura permite diferenciar o Adapter do Decorator, já que o objeto encapsulado normalmente possui um tipo diferente da interface implementada.

#### (b) Quais combinações de elementos configuram o padrão?

O padrão é identificado quando são encontrados:

- uma interface ou classe abstrata;
- uma classe que implementa esse contrato;
- um atributo de outro tipo;
- inicialização desse atributo por construtor;
- métodos que delegam chamadas para esse objeto, podendo adaptar parâmetros ou valores de retorno.

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
        double resultado = elemento.processar(String.valueOf(valor));
        return String.valueOf(resultado);
    }
}
```

---

### Strategy

#### (a) Quais elementos estruturais do código você decidiu observar e por quê?

Para detectar o padrão Strategy, a ferramenta procura uma interface (ou classe abstrata) com duas ou mais implementações concretas. Em seguida, verifica se existe outra classe que mantém uma referência para essa abstração e utiliza esse objeto para executar parte do seu comportamento.

Esses elementos indicam que diferentes implementações podem ser utilizadas sem alterar a classe que as utiliza.

#### (b) Quais combinações de elementos configuram o padrão?

O padrão é identificado quando são encontrados:

- uma interface ou classe abstrata;
- duas ou mais implementações dessa abstração;
- uma classe que possui um atributo desse tipo;
- inicialização desse atributo por construtor, setter ou parâmetro;
- um método que delega a execução para o objeto armazenado.

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

Algumas implementações também podem não ser reconhecidas caso utilizem uma organização muito diferente das estruturas tradicionais. No caso do Strategy, por exemplo, a detecção depende da existência de mais de uma implementação da mesma abstração.

Além disso, o detector não analisa o comportamento do programa em tempo de execução. Implementações baseadas em reflexão, geração dinâmica de objetos, proxies, lambdas ou mecanismos externos de injeção de dependência podem não ser identificadas corretamente.
