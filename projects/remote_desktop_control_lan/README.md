
# LAN-Based Remote Desktop Sharing and Control System

## Description
This project replicates basic AnyDesk-like functionality over a Local Area Network (LAN).  
It enables a server to view and control the client’s desktop in real time.  
UDP is used for screen streaming (for low latency), while TCP handles keyboard and mouse control (for reliability).

---


## Key Features Implemented
-   **Real-time Screen Streaming (UDP):**  
    The client continuously captures the screen, encodes it into JPEG format, and transmits it in chunks using UDP for minimal latency.
    
-   **Reliable Control Channel (TCP):**  
    A separate TCP socket handles control commands such as mouse movement, clicks, and keyboard events.
    
-   **Bidirectional Communication:**  
    The server receives screen data while also sending control signals back to the client.
    
-   **Chunk-based Image Transmission:**  
    The image data is divided into manageable packets, reassembled on the server side, and displayed using Swing UI components.
    
-   **Multi-threaded Operation:**  
    Independent threads handle UDP streaming, TCP command exchange, and GUI updates for smooth and concurrent performance.
    
-   **Cross-platform Support:**  
    Compatible with any system running Java and connected over the same LAN.

---

## Technology Stack
- **Language:** Java (JDK 17+)
- **Libraries:** AWT, Swing, Java Networking APIs
- **Tools:** Wireshark, IntelliJ IDEA, GitHub

---

## Installation and Setup

### Prerequisites
- Java JDK 17 or higher installed
- Two systems connected over the same LAN
- Basic permissions to run Java programs

### Installation Steps

1.  Clone the repository:
    
    `git clone https://github.com/lan-lords/lan-desktop-control.git
    cd lan-desktop-control`
    
2.  Compile the project:
    
    `javac ServerMainUDPWithControl.java
    javac ClientMainUDPWithControl.java` 
    
3.  Run the server:
    
    `java ServerMainUDPWithControl` 
    
    -   Enter the UDP port (e.g., `5000`) when prompted.
    -   The server window will open, ready to display the remote screen.
        
4.  Run the client:

    `java ClientMainUDPWithControl` 

    -   Enter the server’s IP address and the same UDP port used by the server.
    -   The client begins streaming its screen and awaits control commands.
