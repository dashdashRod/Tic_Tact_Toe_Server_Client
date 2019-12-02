package com.tictactoe;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TicTacToe implements Runnable {
	private Socket socket;
	private DataOutputStream dos;
	private DataInputStream dis;
        private Painter painter_user;
	private String ip;
	private int port;
	private Scanner scanner = new Scanner(System.in);
	private JFrame frame;
	private final int WIDTH = 506;
	private final int HEIGHT = 527;
	private Thread thread;
	private ServerSocket serverSocket;

	private BufferedImage board;
	private BufferedImage redX;
	private BufferedImage blueX;
	private BufferedImage redCircle;
	private BufferedImage blueCircle;

	private void Carregar_Imagens() {
		try {   
                        board = ImageIO.read(getClass().getResourceAsStream("/board.png"));
                        blueX = ImageIO.read(getClass().getResourceAsStream("/blueX.png"));
                        blueCircle = ImageIO.read(getClass().getResourceAsStream("/blueCircle.png"));
			redX = ImageIO.read(getClass().getResourceAsStream("/redX.png"));
			redCircle = ImageIO.read(getClass().getResourceAsStream("/redCircle.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String[] spaces = new String[9];
        private int MaxNumeroPortas = 65535;
        private int MinNumeroDePortas = 0;
	private boolean turno = false;
	private boolean adversary = true;
	private boolean accepted = false;
	private boolean Impossivel_Conexao_Adversario = false;
	private boolean VITORIA = false;
	private boolean InimigoGanhou = false;
	private boolean EMPATE = false;
        
        private void Frame_Generator_Method_JFrame(){
            frame = new JFrame();
            frame.setTitle("Tic-Tac-Toe");
            frame.setContentPane(painter_user);
            frame.setSize(WIDTH, HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setVisible(true);
        }
        
        private void Draw_Text_Function(Graphics valor, Font valor_fonte,String String_Mensagem){
            valor.setColor(Color.RED);
            valor.setFont(valor_fonte);
            Graphics2D Graficos_2d = (Graphics2D) valor;
            Graficos_2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int TamanoDaString = Graficos_2d.getFontMetrics().stringWidth(String_Mensagem);
            valor.drawString(String_Mensagem, WIDTH / 2 - TamanoDaString / 2, HEIGHT / 2);
            return;
        }
        
        private void DesenharLinha(int PosicaoX,int PosicaoY,int PosicaoX2,int PosicaoY2,Graphics Grafico){
            if(PosicaoX < 0 || PosicaoX > 500){
                Scanner variavel = new Scanner(System.in);
                int posicao;
                System.out.println("Valor da Primeira Posicao De X Fora da Tela, Inserir Novamente");
                posicao = scanner.nextInt();
                DesenharLinha(posicao, PosicaoY, PosicaoX2, PosicaoY2,Grafico);
            }
            else if(PosicaoY < 0 || PosicaoY > 500){
                Scanner variavel = new Scanner(System.in);
                int posicao;
                System.out.println("A Altura do Valor de Y esta fora da tela, por favor re-digitar ");
                posicao = scanner.nextInt();
                DesenharLinha(PosicaoX, posicao, PosicaoX2, PosicaoY2,Grafico);
            }
            else if(PosicaoX2 < 0 || PosicaoX2 > 500){
                Scanner variavel = new Scanner(System.in);
                int posicao;
                System.out.println("Valor da Segunda Posicao de X Fora da Tela, Inserir Novamente");
                posicao = scanner.nextInt();
                DesenharLinha(PosicaoX, PosicaoY, posicao, PosicaoY2,Grafico);
            }
            else if(PosicaoY2 < 0 || PosicaoY2 > 500){
                Scanner variavel = new Scanner(System.in);
                int posicao;
                System.out.println("A Altura do Valor de Y2 esta fora da tela, por favor re-digitar  ");
                posicao = scanner.nextInt();
                DesenharLinha(PosicaoX, PosicaoY, PosicaoX2, posicao,Grafico);
            }
            Graphics2D Grafico_reta = (Graphics2D) Grafico;
            Grafico_reta.setStroke(new BasicStroke(15));
            Grafico.setColor(Color.BLACK);
            Grafico.drawLine(Calculo_Pontos_Da_RetaXX, Calculo_Pontos_Da_RetaXY, Calculo_Pontos_Da_RetaX2X, Calculo_Pontos_Da_RetaX2Y);
        }
        
	private int tamanhoDosEspacos = 160;
	private int errors = 0;
	private int primeiro_lugar = -1;
	private int segundo_lugar = -1;

	private Font font = new Font("Times New Roman", Font.BOLD, 32);
	private Font smallerFont = new Font("Times New Roman", Font.BOLD, 20);
	private Font largerFont = new Font("Times New Roman", Font.BOLD, 50);

	private String StringDeEspera = "Esperando Jogador";
	private String unableToCommunicateWithOpponentString = "Incp de se comunicar com adversario.";
	private String StringDeVitoria = "Voce Ganhou!";
	private String InimigoGanhouString = "Oponente Ganhou!";
	private String StringDeEmpate = "Jogo termina em empate.";
        private int Calculo_Pontos_Da_RetaXX = (primeiro_lugar % 3 * tamanhoDosEspacos + 10 * primeiro_lugar % 3 + tamanhoDosEspacos / 2);
        private  int Calculo_Pontos_Da_RetaXY = ((int) (primeiro_lugar / 3) * tamanhoDosEspacos + 10 * (int) (primeiro_lugar / 3) + tamanhoDosEspacos / 2);
        private int Calculo_Pontos_Da_RetaX2X = (segundo_lugar % 3 * tamanhoDosEspacos + 10 * segundo_lugar % 3 + tamanhoDosEspacos / 2);
        private int Calculo_Pontos_Da_RetaX2Y = ((int) (segundo_lugar / 3) * tamanhoDosEspacos + 10 * (int) (segundo_lugar / 3) + tamanhoDosEspacos / 2);
	private int[][] Combinacoes_Vitoria = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 }, { 2, 4, 6 } };
        
        
	private void Iniciar_Servidor() {
		try {
			serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
		} catch (Exception e) {
			e.printStackTrace();
		}
		turno = true;
		adversary = false;
	}

        
	public TicTacToe() {
		System.out.println("Por Favor Insira o IP: ");
		ip = scanner.nextLine();
		System.out.println("Por Favor Insira a Porta: ");
		port = scanner.nextInt();
                boolean Resposta = ((port < MinNumeroDePortas || port > MaxNumeroPortas) == true) ? true : false;
		while (Resposta) {
			System.out.println("A porta selecionada e invalida");
                        System.out.println("Por favor Digite um numero de Portas Validas, valores entre 1 e 65535: ");
			port = scanner.nextInt();
                        Resposta = ((port < MinNumeroDePortas || port > MaxNumeroPortas) == true) ? true : false;
                }
		Carregar_Imagens();
		painter_user = new Painter();
		painter_user.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		if (!Connection()){
                    Iniciar_Servidor();
                }
		Frame_Generator_Method_JFrame();
		thread = new Thread(this, "TicTacToe");
		thread.start();
	}
        
        @SuppressWarnings("unused")
	public static void main(String[] args) {
		TicTacToe TicTac = new TicTacToe();
	}

	public void run() {
		while (true) {
			ciclick();
			painter_user.repaint();

			if (!adversary && !accepted) {
				ListenToServer();
			}

		}
	}
        
        
	private void Renderizador_Quadros(Graphics g) {
		g.drawImage(board, 0, 0, null);
		if (Impossivel_Conexao_Adversario) {
                        Draw_Text_Function(g,new Font("Times New Roman", Font.BOLD, 20), unableToCommunicateWithOpponentString);
		}

		if (accepted) {
			for (int i = 0; i < spaces.length; i++) {
                                int calculo_valor_x = (i % 3) * (160) + 10 * (i % 3);
                                int calculo_valor_y = (int) (i / 3) * 160 + 10 * (int) (i / 3);
                                if (spaces[i] != null) {
					if (spaces[i].equals("X")) {
						if (adversary) {
                                                        g.drawImage(redX, calculo_valor_x,calculo_valor_y, null);
						} else {
							g.drawImage(blueX,calculo_valor_x,calculo_valor_y, null);
						}
					} else if (spaces[i].equals("O")) {
						if (adversary) {
							g.drawImage(blueCircle,calculo_valor_x,calculo_valor_y, null);
						} else {
							g.drawImage(redCircle,calculo_valor_x,calculo_valor_y, null);
						}
					}
				}
			}
			if (VITORIA || InimigoGanhou) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(10));
				g.setColor(Color.BLACK);
				g.drawLine(primeiro_lugar % 3 * tamanhoDosEspacos + 10 * primeiro_lugar % 3 + tamanhoDosEspacos / 2, (int) (primeiro_lugar / 3) * tamanhoDosEspacos + 10 * (int) (primeiro_lugar / 3) + tamanhoDosEspacos / 2, segundo_lugar % 3 * tamanhoDosEspacos + 10 * segundo_lugar % 3 + tamanhoDosEspacos / 2, (int) (segundo_lugar / 3) * tamanhoDosEspacos + 10 * (int) (segundo_lugar / 3) + tamanhoDosEspacos / 2);

				g.setColor(Color.RED);
				g.setFont(largerFont);
				if (VITORIA) {
					int stringWidth = g2.getFontMetrics().stringWidth(StringDeVitoria);
					g.drawString(StringDeVitoria, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
				} else if (InimigoGanhou) {
					int stringWidth = g2.getFontMetrics().stringWidth(InimigoGanhouString);
					g.drawString(InimigoGanhouString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
				}
			}
			if (EMPATE) {
				Graphics2D g2 = (Graphics2D) g;
				g.setColor(Color.BLACK);
				g.setFont(largerFont);
				int stringWidth = g2.getFontMetrics().stringWidth(StringDeEmpate);
				g.drawString(StringDeEmpate, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			}
		} else {
			g.setColor(Color.RED);
			g.setFont(new Font("Times New Roman", Font.BOLD, 32));
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth("Esperando Jogador");
			g.drawString("Esperando Jogador", WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
		}

	}
        
	private void ciclick() {
		if (!turno && !Impossivel_Conexao_Adversario) {
			try {
				int space = dis.readInt();
				if (adversary){
                                    spaces[space] = "X";
                                }
                                else{
                                    spaces[space] = "O";
                                }
				CheckVitoriaAdversario();
				ChecarEmpate();
				turno = true;
			} catch (IOException e) {
				e.printStackTrace();
				errors++;
			}
		}
                
		if (errors >= 10){
                    Impossivel_Conexao_Adversario = true;
                }

	}

	private void ChecarVitoria() {
		for (int i = 0; i < Combinacoes_Vitoria.length; i++) {
			if (adversary) {
				if (spaces[Combinacoes_Vitoria[i][0]] == "O" && spaces[Combinacoes_Vitoria[i][1]] == "O" && spaces[Combinacoes_Vitoria[i][2]] == "O") {
					primeiro_lugar = Combinacoes_Vitoria[i][0];
					segundo_lugar = Combinacoes_Vitoria[i][2];
					VITORIA = true;
				}
			} else {
				if (spaces[Combinacoes_Vitoria[i][0]] == "X" && spaces[Combinacoes_Vitoria[i][1]] == "X" && spaces[Combinacoes_Vitoria[i][2]] == "X") {
					primeiro_lugar = Combinacoes_Vitoria[i][0];
					segundo_lugar = Combinacoes_Vitoria[i][2];
					VITORIA = true;
				}
			}
		}
	}

	private void CheckVitoriaAdversario() {
		for (int i = 0; i < Combinacoes_Vitoria.length; i++) {
			if (adversary) {
				if (spaces[Combinacoes_Vitoria[i][0]] == "X" && spaces[Combinacoes_Vitoria[i][1]] == "X" && spaces[Combinacoes_Vitoria[i][2]] == "X") {
					primeiro_lugar = Combinacoes_Vitoria[i][0];
					segundo_lugar = Combinacoes_Vitoria[i][2];
					InimigoGanhou = true;
				}
			} else {
				if (spaces[Combinacoes_Vitoria[i][0]] == "O" && spaces[Combinacoes_Vitoria[i][1]] == "O" && spaces[Combinacoes_Vitoria[i][2]] == "O") {
					primeiro_lugar = Combinacoes_Vitoria[i][0];
					segundo_lugar = Combinacoes_Vitoria[i][2];
					InimigoGanhou = true;
				}
			}
		}
	}

	private void ChecarEmpate() {
		for (int i = 0; i < spaces.length; i++) {
			if (spaces[i] == null) {
				return;
			}
		}
		EMPATE = true;
	}

	private void ListenToServer() {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
                        try{
			dos = new DataOutputStream(socket.getOutputStream());
                        }
                        catch (IOException e){
                            System.out.println("Erro com o OutPut do LitenToServer");
                            System.out.println(e.toString());
                        }
                        try{
                        dis = new DataInputStream(socket.getInputStream());
                        }
                        catch (IOException e){
                            System.out.println("Erro com o InPut do LitenToServer");
                            System.out.println(e.toString());
                        }
                        accepted = true;
			System.out.println("CLIENTE FEZ SOLICITACAO PARA SE JUNTAR, SOLICITACAO ACEITA");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean Connection() {
		try {
			socket = new Socket(ip, port);
			try{
                        dos = new DataOutputStream(socket.getOutputStream());
                        }
                        catch(IOException e){
                            System.out.println("Erro Com o OutPut do Connection");
                            System.out.println(e.toString());
                        }
                        try{
                        dis = new DataInputStream(socket.getInputStream());
                        }
                        catch (IOException e){
                            System.out.println("Erro Com o Input do Connection");
                            System.out.println(e.toString());
                        }
                        accepted = true;
		} catch (IOException e) {
			System.out.println("Incapas de se conectar ao servidor no endereco: " + ip + ":" + port + " | Iniciando o Servidor");
			return false;
		}
		System.out.println("Conectado Ao Server Com Sucesso.");
		return true;
	}

	private class Painter extends JPanel implements MouseListener {
		private static final long serialVersionUID = 1L;

		public Painter() {
			setFocusable(true);
			requestFocus();
			setBackground(Color.WHITE);
			addMouseListener(this);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Renderizador_Quadros(g);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		if (accepted) {
                    if (turno) {
                        boolean variavel = !Impossivel_Conexao_Adversario;
                        boolean Conclusao = (!VITORIA && !InimigoGanhou);
                        if (Conclusao == true) {
                            int new_Variable = (variavel && Conclusao) == true ? 1 : 0;
                            if (variavel == true && new_Variable == 1) {
                                int x = e.getX() / tamanhoDosEspacos;
                                int y = e.getY() / tamanhoDosEspacos;
                                y *= 3;
                                int position = x + y;

                                if (spaces[position] == null) {
                                    if (!adversary) {
                                        spaces[position] = "X";
                                    } else {
                                        spaces[position] = "O";
                                    }
                                    turno = false;
                                    repaint();
                                    Toolkit.getDefaultToolkit().sync();

                                    try {
                                        dos.writeInt(position);
                                        dos.flush();
                                    } catch (IOException e1) {
                                        errors++;
                                        e1.printStackTrace();
                                    }
                                    ChecarVitoria();
                                    ChecarEmpate();

                                }
                        }
                    }
                }
            }
        }
		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

	}

}
