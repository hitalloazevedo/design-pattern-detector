<style>
@page {
  size: A4;
  margin: 18mm 16mm 18mm 16mm;
}

html, body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, sans-serif;
  font-size: 11pt;
  line-height: 1.45;
  color: #111;
  background: #fff;
}

body {
  max-width: 100%;
  margin: 0;
  padding: 0;
}

h1 {
  font-size: 24pt;
  margin: 0 0 14pt;
  line-height: 1.15;
}

h2 {
  font-size: 17pt;
  margin-top: 22pt;
  padding-bottom: 4pt;
  border-bottom: 1px solid #bbb;
  page-break-after: avoid;
}

h3 {
  font-size: 14pt;
  margin-top: 18pt;
  page-break-after: avoid;
}

h4 {
  font-size: 12pt;
  margin-top: 14pt;
  page-break-after: avoid;
}

p, li {
  orphans: 3;
  widows: 3;
}

ul, ol {
  padding-left: 22px;
}

hr {
  border: 0;
  border-top: 1px solid #bbb;
  margin: 20pt 0;
}

code {
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
  font-size: 9pt;
}

pre {
  white-space: pre-wrap !important;
  overflow-wrap: anywhere;
  word-break: break-word;
  overflow-x: visible !important;
  max-width: 100%;
  padding: 10pt;
  border: 1px solid #ccc;
  border-radius: 4px;
  background: #f6f6f6;
  page-break-inside: avoid;
}

pre code {
  white-space: pre-wrap !important;
  overflow-wrap: anywhere;
  word-break: break-word;
}

a {
  color: #111;
  text-decoration: underline;
}

table {
  width: 100%;
  border-collapse: collapse;
  page-break-inside: avoid;
}

th, td {
  border: 1px solid #bbb;
  padding: 6pt;
  vertical-align: top;
}

blockquote {
  margin: 10pt 0;
  padding-left: 12pt;
  border-left: 3px solid #999;
}

.page-break {
  break-before: page;
  page-break-before: always;
}

.no-break {
  break-inside: avoid;
  page-break-inside: avoid;
}

@media print {
  html, body {
    width: 100%;
  }

  pre, code {
    white-space: pre-wrap !important;
    overflow-wrap: anywhere !important;
    word-break: break-word !important;
  }

  h1, h2, h3, h4 {
    break-after: avoid;
  }

  a[href]::after {
    content: none !important;
  }
}
</style>

<div class="no-break">

# Detector Estático de Padrões de Design

**Repositório:** <https://github.com/hitalloazevedo/design-pattern-detector>

A ferramenta realiza análise estática da estrutura do código-fonte utilizando a Árvore Sintática Abstrata (AST) e resolução de símbolos, sem executar o programa. A detecção é baseada apenas na estrutura das classes e em seus relacionamentos.

</div>

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

<div class="page-break"></div>

## Exemplos de execução

### 1. Decorator com nomes genéricos

Este exemplo utiliza nomes genéricos para demonstrar que a detecção não depende do nome das classes, métodos ou atributos.

#### Código de entrada

**A.java**

```java
package samples.generic;

public interface A {
    void run();
}
```

**B.java**

```java
package samples.generic;

public final class B implements A {

    @Override
    public void run() {
        System.out.println("Base");
    }
}
```

**C.java**

```java
package samples.generic;

public final class C implements A {

    private final A value;

    public C(A value) {
        this.value = value;
    }

    @Override
    public void run() {
        System.out.println("Before");
        value.run();
        System.out.println("After");
    }
}
```

#### Comando

```bash
mvn exec:java -Dexec.args="samples/generic"
```

#### Saída gerada

```text
[PADRÃO DETECTADO] DECORATOR
Elementos identificados:
- A classe samples.generic.C implementa ou estende samples.generic.A. — samples/generic/C.java:3
- A classe possui o atributo de instância value do tipo samples.generic.A. — samples/generic/C.java:5
- O parâmetro value é armazenado no atributo value. — samples/generic/C.java:8
- Uma operação é delegada para o atributo value por meio da chamada value.run(...). — samples/generic/C.java:14
Vantagem neste contexto: A classe samples.generic.C pode adicionar comportamento ao contrato samples.generic.A sem modificar o objeto armazenado no atributo value. Como ambos seguem o mesmo contrato, os objetos podem ser combinados em cadeia.
Risco/desvantagem neste contexto: A classe samples.generic.C adiciona uma nova camada de delegação por meio do atributo value. O uso de várias camadas semelhantes pode dificultar o acompanhamento da ordem das chamadas. Foi encontrada 1 chamada delegada para esse atributo.
```

A classe `C` implementa a abstração `A`, armazena outro objeto do mesmo tipo e delega a operação `run()` para ele. A identificação ocorre mesmo sem nomes que revelem o padrão.

---

<div class="page-break"></div>

### 2. Adapter

#### Código de entrada

**Adapter.java**

```java
package samples.adapter;

public interface Adapter {
    void adapt();
}
```

**Legacy.java**

```java
package samples.adapter;

public final class Legacy {

    public void legacyMethod() {
        System.out.println("Legacy");
    }
}
```

**ConcreteAdapter.java**

```java
package samples.adapter;

public final class ConcreteAdapter implements Adapter {

    private final Legacy legacy;

    public ConcreteAdapter(Legacy legacy) {
        this.legacy = legacy;
    }

    @Override
    public void adapt() {
        this.legacy.legacyMethod();
        System.out.println("Adapted");
    }
}
```

#### Comando

```bash
mvn exec:java -Dexec.args="samples/adapter"
```

#### Saída gerada

```text
[PADRÃO DETECTADO] ADAPTER
Elementos identificados:
- A classe samples.adapter.ConcreteAdapter implementa ou estende o contrato samples.adapter.Adapter. — samples/adapter/ConcreteAdapter.java:3
- A classe possui o atributo de instância legacy do tipo samples.adapter.Legacy, diferente do contrato implementado. — samples/adapter/ConcreteAdapter.java:5
- O parâmetro legacy é armazenado no atributo legacy. — samples/adapter/ConcreteAdapter.java:8
- A classe encaminha uma operação para o atributo legacy por meio da chamada this.legacy.legacyMethod(...). — samples/adapter/ConcreteAdapter.java:13
Vantagem neste contexto: A classe samples.adapter.ConcreteAdapter permite que um objeto do tipo samples.adapter.Legacy seja utilizado por meio do contrato samples.adapter.Adapter. Dessa forma, o código cliente pode trabalhar com a abstração esperada sem depender diretamente do tipo adaptado.
Risco/desvantagem neste contexto: A classe samples.adapter.ConcreteAdapter adiciona uma camada intermediária entre o contrato samples.adapter.Adapter e o tipo samples.adapter.Legacy. Além disso, uma estrutura semelhante também pode ocorrer em classes de serviço que delegam operações para uma dependência, o que pode gerar falsos positivos.
```

A classe `ConcreteAdapter` implementa o contrato esperado pelo cliente, mas encaminha a execução para um objeto de outro tipo.

---

<div class="page-break"></div>

### 3. Strategy

#### Código de entrada

**Strategy.java**

```java
package samples.strategy;

public interface Strategy {
    void execute();
}
```

**ConcreteA.java**

```java
package samples.strategy;

public final class ConcreteA implements Strategy {

    @Override
    public void execute() {
        System.out.println("A");
    }
}
```

**ConcreteB.java**

```java
package samples.strategy;

public final class ConcreteB implements Strategy {

    @Override
    public void execute() {
        System.out.println("B");
    }
}
```

**Orchestrator.java**

```java
package samples.strategy;

public final class Orchestrator {

    private Strategy strategy;

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void executeStrategy() {
        strategy.execute();
    }
}
```

#### Comando

```bash
mvn exec:java -Dexec.args="samples/strategy"
```

#### Saída gerada

```text
[PADRÃO DETECTADO] STRATEGY
Elementos identificados:
- samples.strategy.Strategy é uma abstração com 2 implementações concretas. — samples/strategy/Strategy.java:3
- A classe samples.strategy.ConcreteA implementa ou estende samples.strategy.Strategy. — samples/strategy/ConcreteA.java:3
- A classe samples.strategy.ConcreteB implementa ou estende samples.strategy.Strategy. — samples/strategy/ConcreteB.java:3
- A classe samples.strategy.Orchestrator possui o atributo de instância strategy do tipo samples.strategy.Strategy. — samples/strategy/Orchestrator.java:4
- O parâmetro strategy é armazenado no atributo strategy. — samples/strategy/Orchestrator.java:8
- A classe samples.strategy.Orchestrator delega uma operação para o atributo strategy por meio da chamada strategy.execute(...). — samples/strategy/Orchestrator.java:12
Vantagem neste contexto: A classe samples.strategy.Orchestrator pode alterar o comportamento utilizado ao substituir o objeto armazenado no atributo strategy. Foram encontradas 2 implementações concretas de samples.strategy.Strategy, permitindo a troca do comportamento sem modificar a classe de contexto.
Risco/desvantagem neste contexto: A estrutura envolve a classe de contexto samples.strategy.Orchestrator, a abstração samples.strategy.Strategy e 2 implementações concretas. Para compreender o comportamento executado, é necessário identificar qual implementação foi atribuída ao atributo strategy.
```

A abstração possui duas implementações concretas e a classe de contexto mantém uma referência substituível para essa abstração.


---

<div class="page-break"></div>

## Limitações
A ferramenta utiliza heurísticas estruturais e, por isso, pode produzir falsos positivos quando classes comuns apresentam relações semelhantes às de um padrão. O Adapter pode ser confundido com serviços que implementam uma interface e delegam operações para uma dependência. Implementações que não utilizam delegação explícita podem não ser detectadas. O Strategy exige pelo menos duas implementações concretas da abstração. A ferramenta não analisa o comportamento em tempo de execução. Reflexão, proxies, classes geradas dinamicamente, lambdas e mecanismos externos de injeção de dependência não são tratados. A resolução de tipos também pode ser limitada quando as dependências externas do projeto não estão disponíveis.
