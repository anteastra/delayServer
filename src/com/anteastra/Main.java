package com.anteastra;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Throwable {

        if (args.length < 3) {
            System.out.print("hlp: java app port delay_ms file_path");
        }

        int port = Integer.valueOf(args[0]);
        int delayMs = Integer.valueOf(args[1]);
        String filePath = args[2];

        ServerSocket ss = new ServerSocket(port);
        while (true) {
            Socket s = ss.accept();
            System.err.println("Client accepted");
            new Thread(new SocketProcessor(s, delayMs, filePath)).start();
        }

    }

    private static class SocketProcessor implements Runnable {

        private Socket s;
        private InputStream is;
        private OutputStream os;
        private int delay;
        private String filePath;

        private SocketProcessor(Socket s, int delay, String filePath) throws Throwable {
            this.s = s;
            this.is = s.getInputStream();
            this.os = s.getOutputStream();
            this.delay = delay;
            this.filePath = filePath;
        }

        public void run() {
            try {
                System.out.println(s.getInetAddress());
                System.out.println(s.getLocalAddress());
                readInputHeaders();
                Thread.sleep(delay);
                writeResponse();
            } catch (Throwable t) {
                /*do nothing*/
            } finally {
                try {
                    s.close();
                } catch (Throwable t) {
                    /*do nothing*/
                }
            }
            System.err.println("Client processing finished");
        }

        private void writeResponse() throws Throwable {

            File toBeCopied = new File(filePath);
            Path path = toBeCopied.toPath();

            String response = "HTTP/1.1 200 OK\r\n" +
                    "Server: DelayFileServer\r\n" +
                    "Content-Type: application/octet-stream\r\n" +
                    "Content-Length: " + toBeCopied.length() + "\r\n" +
                    "Connection: close\r\n\r\n";
            String result = response + s;
            Files.copy(path, os);
            os.flush();
        }

        private void readInputHeaders() throws Throwable {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String s = br.readLine();
                if (s == null || s.trim().length() == 0) {
                    break;
                }
            }
        }
    }
}
