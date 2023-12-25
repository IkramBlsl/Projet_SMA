## Consumer Producer Agent System

The Consumer Producer Agent System simulates an environment where agents consume and produce different types of merchandise. The agents interact with each other by buying, selling, and monitoring satisfaction levels based on their consumption and production.
[projetSMA.pdf](https://github.com/IkramBlsl/Projet_SMA/files/13692527/projetSMA.pdf)

## Features

- Agent Interaction: Agents can communicate, buy, and sell merchandise among each other.
- Consumption & Production: Agents can consume and produce various types of merchandise.
- Satisfaction Management: Agents' satisfaction levels change based on their consumption and trade experiences.
- Dynamic Behavior: The system provides cyclic behaviors to simulate production, consumption, and trade decisions.


## Requirements

    Java Development Kit (JDK)
    JADE (Java Agent DEvelopment Framework)


## Usage

Setup:
        Ensure JDK is installed and set up.
        Download and install JADE from [JADE](https://jade.tilab.com/maven/com/tilab/jade/jade/4.5.0/jade-4.5.0.jar).

Execution:
        Compile the Java files using javac command or preferred IDE.
        Run the JADE platform with the main container.
        Run the agents by starting the main agent classes.

Interact:
        Observe the interactions between agents in the console output.
        Agents will engage in buying, selling, producing, and consuming merchandise.

## Code Structure

- ConsumerProducerAgent.java: The primary agent overseeing consumption, production, and trade within the system.    
- BuyConsumedProductBehaviour.java: This class defines a behavior representing the process of purchasing consumed merchandise from producers. It compares the propositions received from producers and accepts the best offer for the purchase.
- ConsumerBehaviour.java: This behavior class is designed for the Consumer agent. It operates cyclically, regularly checking the stock of the consumed merchandise. If sufficient stock is available, it consumes the merchandise, updating the agent's satisfaction. Otherwise, it decides to purchase from a producer, reducing the agent's satisfaction.
- ProducerBehaviour.java: This behavior class is designed for the Producer agent. It operates cyclically, continuously checking if there is available space in the produced merchandise stock. When space is available, it initiates the production of merchandise, adding the produced goods to the stock of merchandise sold by the agent.
- Proposition.java: Represents a proposition made by a producer to sell merchandise. It encapsulates essential details like the sender (AID), the specific merchandise offered, the quantity, and the price associated with the proposition.
- SellProducedProductBehaviour.java: This behavior manages the sale of produced merchandise. It listens for Call for Proposals (CFP) messages from consumers, responds with propositions, handles the acceptance or rejection of these propositions, and conducts the sale of merchandise accordingly.
