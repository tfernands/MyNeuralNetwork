package main;


class Count extends Thread {

    Count() {
        System.out.println("my thread created" + this);
        start();
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getId());
        while(true){
            if (Math.random() < 0.0000001)
                MyNeuralNetwork.Number.number++;
        }
    }
}
