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

* A melhor parte disso tudo, é sua simplicidade em todos os passos: o princípio é fazer com que os indivíduos mais aptos gerem descendentes, que se tornarão aptos e farão o mesmo para as próximas gerações. Esses indivíduos serão colocados em uma mating pool, uma piscina genética, de onde serão sorteados os pares reprodutivos.

* Isso é feito calculando uma nota para cada indivíduo, que, no nosso caso é dada pelo inverso da distância ao alvo dividida pelo tempo de fim e, ao final, elevada à quarta para que o resultado fique exponencial; além disso, se o indivíduo consegue chegar no alvo, a sua nota é dobrada; e se colide com alguma parede, perde 90% da nota. Essa função é assim porque damos importância tanto para a chegada ao objetivo quanto para o caminho feito (não deve haver colisões).

# Funcionamento

<p align="center">
  <img src="https://github.com/GabrielScalici/Rockets_Evolutivos/blob/master/visualization/animation.gif"/>
</p>
