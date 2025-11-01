import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.Scanner;

public class ClientMainUDPWithControl {
    private String serverIp;
    private int udpPort;
    private int controlPort = 6000;
    private Robot robot;
    private Rectangle screenRect;
    private Socket controlSocket;

    public static void main(String[] args) throws Exception {
        String ip = JOptionPane.showInputDialog("Enter server IP:");
        String udpPortStr = JOptionPane.showInputDialog("Enter server UDP port:");
        int udpPort = Integer.parseInt(udpPortStr);

        new ClientMainUDPWithControl(ip, udpPort).startClient();
    }

    public ClientMainUDPWithControl(String ip, int udpPort) throws AWTException {
        this.serverIp = ip;
        this.udpPort = udpPort;
        this.robot = new Robot();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenRect = new Rectangle(screenSize);
    }

    public void startClient() throws IOException {
        // Connect to server TCP control port (for receiving control commands)
        controlSocket = new Socket(serverIp, controlPort);
        new Thread(new ControlCommandListener(controlSocket, robot)).start();

        // Start sending screenshots over UDP continuously
        DatagramSocket udpSocket = new DatagramSocket();
        InetAddress serverAddress = InetAddress.getByName(serverIp);

        while (true) {
            BufferedImage screenshot = robot.createScreenCapture(screenRect);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenshot, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            int chunkSize = 32000;
            int totalChunks = (int) Math.ceil(imageBytes.length / (double) chunkSize);
            int imageId = (int) (System.currentTimeMillis() & 0xfffffff);

            for (int i = 0; i < totalChunks; i++) {
                int start = i * chunkSize;
                int length = Math.min(chunkSize, imageBytes.length - start);
                byte[] chunkData = new byte[length];
                System.arraycopy(imageBytes, start, chunkData, 0, length);

                ByteArrayOutputStream baosPacket = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baosPacket);
                dos.writeInt(imageId);
                dos.writeInt(totalChunks);
                dos.writeInt(i);
                dos.writeInt(length);
                dos.write(chunkData);
                byte[] packetData = baosPacket.toByteArray();

                DatagramPacket packet = new DatagramPacket(packetData, packetData.length, serverAddress, udpPort);
                udpSocket.send(packet);
            }

            try {
                Thread.sleep(100); // adjust for smooth screen updates and bandwidth
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Thread class to receive control commands and execute using Robot
    class ControlCommandListener implements Runnable {
        private Socket socket;
        private Robot robot;

        public ControlCommandListener(Socket socket, Robot robot) {
            this.socket = socket;
            this.robot = robot;
        }

        public void run() {
            try {
                Scanner scanner = new Scanner(socket.getInputStream());
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int clientScreenWidth = screenSize.width;
                int clientScreenHeight = screenSize.height;
                while (true) {
                    if (!scanner.hasNext()) continue;
                    String token = scanner.next();
                    int command = Integer.parseInt(token);
                    double sensitivity = 5; 
                    switch (command) {
                        case -1: // mouse press
                            int pressBtn = scanner.nextInt();
                            robot.mousePress(pressBtn);
                            break;
                        case -2: // mouse release
                            int releaseBtn = scanner.nextInt();
                            robot.mouseRelease(releaseBtn);
                            break;
                        case -3: // key press
                            int keyPress = scanner.nextInt();
                            robot.keyPress(keyPress);
                            break;
                        case -4: // key release
                            int keyRelease = scanner.nextInt();
                            robot.keyRelease(keyRelease);
                            break;
                        case -5: // mouse move (relative)
                            double relX = scanner.nextDouble();
                            double relY = scanner.nextDouble();
                            int absX = (int) (relX * clientScreenWidth * sensitivity + (1 - sensitivity) * MouseInfo.getPointerInfo().getLocation().x);
                            int absY = (int) (relY * clientScreenHeight * sensitivity + (1 - sensitivity) * MouseInfo.getPointerInfo().getLocation().y);
                            robot.mouseMove(absX, absY);
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}