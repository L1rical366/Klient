import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.*;

 public class Server extends JFrame {
     private byte board[];
     private boolean xMove;
     private JTextArea output;
     private Player players[];
     private ServerSocket server;
     private int currentPlayer;
     public Server()
     {
         super( "Server" );
         board = new byte[ 9 ];
         xMove = true;
         players = new Player[ 2 ];
         currentPlayer = 0;

         try
         {
             server = new ServerSocket( 1500, 2 );
         }

         catch( IOException e )
         {

             e.printStackTrace();

             System.exit( 1 );

         }

         output = new JTextArea();

         getContentPane().add( output, BorderLayout.CENTER );

         output.setText( "Server awaiting connections\n" );

         setSize( 300, 300 );

         show();

           }

           public void execute()
           {

              for ( int i = 0; i < players.length; i++ ) {

                     try {
                         players[ i ] =
                                 new Player( server.accept(), this, i );

                         players[ i ].start();
                     }
                     catch( IOException e )
                     {

                            e.printStackTrace();
                            System.exit( 1 );
                     }
              }


              synchronized ( players[ 0 ] ) {

                  players[ 0 ].threadSuspended = false;

                  players[ 0 ].notify();

                  }
           }

           public void display( String s )
            {

              output.append( s + "\n" );
          }

   public synchronized boolean validMove( int loc, int player )
    {
        boolean moveDone = false;

        while ( player != currentPlayer ) {

            try
            {
                wait();
            }

            catch( InterruptedException e )
            {
                e.printStackTrace();

            }

        }



             if ( !isOccupied( loc ) ) {

                     board[ loc ] =

                                (byte) ( currentPlayer == 0 ? 'X' : 'O' );

                     currentPlayer = ( currentPlayer + 1 ) % 2;

                     players[ currentPlayer ].otherPlayerMoved( loc );

                    notify();    // tell waiting player to continue

                      return true;

             }

             else

                 return false;

           }
 


           public boolean isOccupied( int loc )
 
    {

              if ( board[ loc ] == 'X' || board [ loc ] == 'O' )

                     return true;

               else

                   return false;

           }
 


           public boolean gameOver()
 
   {



               return false;

            }
 


          public static void main( String args[] )
 
  {

               Server game = new Server();



              game.addWindowListener( new WindowAdapter() {
 
        public void windowClosing( WindowEvent e )
          {

                                                                System.exit( 0 );

                                                            }
 
         }

                       );



               game.execute();

            }
 
 }
 




         class Player extends Thread {
 
    private Socket connection;
 
    private DataInputStream input;
 
    private DataOutputStream output;
 
    private Server control;
 
    private int number;
 
    private char mark;

    protected boolean threadSuspended = true;
 


            public Player( Socket s, Server t, int num )
 
    {

               mark = ( num == 0 ? 'X' : 'O' );



               connection = s;



               try {

                      input = new DataInputStream(

                                         connection.getInputStream() );

                      output = new DataOutputStream(

                                         connection.getOutputStream() );

                   }

               catch( IOException e ) {

                      e.printStackTrace();

                      System.exit( 1 );

                   }



               control = t;

               number = num;

            }
 


            public void otherPlayerMoved( int loc )
 
    {

               try {

                      output.writeUTF( "Opponent moved" );

                      output.writeInt( loc );

                   }

               catch ( IOException e ) { e.printStackTrace(); }

            }
 


            public void run()
 
    {

               boolean done = false;



               try {

                      control.display( "Player " +

                                 ( number == 0 ? 'X' : 'O' ) + " connected" );

                      output.writeChar( mark );

                     output.writeUTF( "Player " +

                              ( number == 0 ? "X connected\n" :

                                            "O connected, please wait\n" ) );





                    if ( mark == 'X' ) {

                           output.writeUTF( "Waiting for another player" );



                           try {

                                 synchronized( this ) {

                                         while ( threadSuspended )

                                                 wait();

                                       }

                               }

                            catch ( InterruptedException e ) {

                                  e.printStackTrace();

                                }

                          output.writeUTF(

                        "Other player connected. Your move." );

                         }



                     // Play game

                      while ( !done ) {

                            int location = input.readInt();



                if ( control.validMove( location, number ) ) {

                                   control.display( "loc: " + location );

                                   output.writeUTF( "Valid move." );

                               }

                            else

                                output.writeUTF( "Invalid move, try again" );



                           if ( control.gameOver() )

                                   done = true;

                        }



                      connection.close();

                 }

              catch( IOException e ) {

                      e.printStackTrace();

                    System.exit( 1 );

                   }

         }

}