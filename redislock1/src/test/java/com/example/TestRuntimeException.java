package com.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestRuntimeException {

    static CountDownLatch finish = new CountDownLatch(2);
    public static void main(String[] args) {
        Thread thread_1 = new Runtime("thread-1");
        Thread thread_2 = new Runtime("thread-2");
        thread_1.start();
        thread_2.start();
        try {
            finish.await();
        } catch (InterruptedException e) {
            System.out.println("InterruptedException.");
        }
        System.out.println("main ends.");
    }


    static class Runtime extends Thread {
        private String name;

        public Runtime(String name) {
            super(name);
        }

        public void run() {
            System.out.println(Thread.currentThread().getName() + " starts.");
            if (Thread.currentThread().getName().equals("thread-1")) {
                finish.countDown();
                throw new RuntimeException();
            }
            try {
                TimeUnit.MINUTES.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            System.out.println(Thread.currentThread().getName() + " ends.");
            finish.countDown();
        }
    }

}
