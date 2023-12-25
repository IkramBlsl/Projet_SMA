## Consumer Producer Agent System

The Consumer Producer Agent System simulates an environment where agents consume and produce different types of products. The agents interact with each other by buying, selling, and monitoring satisfaction levels based on their consumption and production.
[projetSMA.pdf](https://github.com/IkramBlsl/Projet_SMA/files/13692527/projetSMA.pdf)

## Features

- Agent Interaction: Agents can communicate, buy, and sell products among each other.
- Consumption & Production: Agents can consume and produce various types of products.
- Satisfaction Management: Agents' satisfaction levels change based on their consumption and trade experiences.
- Dynamic Behavior: The system provides cyclic behaviors to simulate production, consumption, and trade decisions.


## Requirements

    Java Development Kit (JDK)
    JADE (Java Agent DEvelopment Framework)



## Documentation
- The source code is commented and the Javadoc is generated. If you have to generate it again, you can type `javadoc -cp [path to jade] -d ./doc src/*` given that every source file is in the src directory. 


## Execution
- You can compile the program using this command: javac -cp ./lib/jade.jar -d ./out ./src/*.java
- You can launch the execution using this command: java -cp ./lib/jade.jar:./out jade.Boot -gui -agents s:SimulationAgent
