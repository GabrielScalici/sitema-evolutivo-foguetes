<p align="center">
  <img src="https://media.giphy.com/media/3oKIPtjElfqwMOTbH2/giphy.gif"/>
  <h1 align="center"> Sistema Evolutivo - Foguetes Inteligentes </h1>
  <p align="center"> ICMC - USP  </p>
</p>

<p align="center">
  <img src="https://forthebadge.com/images/badges/made-with-java.svg"/>
</p>


# Projeto

* Como indivíduos aleatoriamente gerados podem chegar até um objetivo não colidindo com obstáculos?

* O problema abordado no trabalho é uma população de foguetes que possui um motor e aplica forças de formas aleatórias em seus corpos e um ponto final onde devem pousar; mas há paredes entre eles e o ponto final e elas precisam ser contornadas. Após as primeiras falhas, a ideia é que nossos indivíduos consigam evoluir e ficar mais inteligentes.

# Referência

* https://www.youtube.com/watch?v=bGz7mv2vD6g&t=90s

# Como rodar

* Para executar o programa, basta baixar o ambiente Processing (https://processing.org/download/) e abrir os arquivos .pde com ele.

# Algoritmo

* Os indivíduos serão colocados em uma mating pool, uma piscina genética, de onde serão sorteados os pares reprodutivos.

* Isso é feito calculando uma nota para cada indivíduo, que, no nosso caso é dada pelo inverso da distância ao alvo dividida pelo tempo de fim e, ao final, elevada à quarta para que o resultado fique exponencial; além disso, se o indivíduo consegue chegar no alvo, a sua nota é dobrada; e se colide com alguma parede, perde 90% da nota. Essa função é assim porque damos importância tanto para a chegada ao objetivo quanto para o caminho feito (não deve haver colisões).

* O método de reprodução escolhido foi o crossover, em que se utiliza parte dos genes da mãe e parte dos genes do pai para gerar o filho. Além disso, quando o código genético do filho está pronto, há a chance de existir uma mutação, que alteraria um campo do código genético para algo novo.

* Começamos toda uma população como tendo características aleatórias, precisamos de sorte para que suas configurações não sejam muito ruins. Quanto maior a sorte nas configurações iniciais, mais rápido será.

* Por questão da linguagem, devemos rodar todo o algoritmo antes, no setup, e fazer a visualização depois.

# Funcionamento

<p align="center">
  <img src="https://github.com/GabrielScalici/Rockets_Evolutivos/blob/master/visualization/animation.gif"/>
</p>
