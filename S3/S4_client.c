/*
 * This is sample code generated by rpcgen.
 * These are only templates and you can use them
 * as a guideline for developing your own functions.
 */

#include "S4.h"

WINDOW *winInput, *winChat;

void *refreshChat(){
  CLIENT *clnt;
	char *getchat_1_arg;

  // Loop infinit cada segon
  while(1){
    #ifndef	DEBUG
    	clnt = clnt_create ("localhost", PROGRAM, VERSION, "udp");
    	if (clnt == NULL) {
    		clnt_pcreateerror ("localhost");
    		exit (1);
    	}
    #endif	/* DEBUG */

    // Demanem el chat en el rpc
    result = getchat_1((void*)&getchat_1_arg, clnt);
  	if (result == (char **) NULL) {
  		clnt_perror (clnt, "call failed");
  	}

    #ifndef	DEBUG
    	clnt_destroy (clnt);
    #endif	 /* DEBUG */


    // Netejem finestra
  	wclear(winChat);
    // Afegim \n al final TODO: substituir per \n al sistema
  	char *temp = (char *)malloc(sizeof(char)*strlen(result[0]) + 2);
  	sprintf(temp, "%s\n", result[0]);
    // Escrivim el chat
  	wprintw(winChat, temp);
  	wscrl(winChat, 1);//?
    // Refresh de la finestra
  	wrefresh(winChat);

    // Esperem un segon
  	sleep(1);
  }
  return NULL;
}

void sortida(int signum){
  // Tanquem les finestres de ncurses
	delwin(winInput);
	delwin(winChat);
  // Tanquem ncurses
	endwin();
  // Acabem el programa.
	exit(0);
}

void program_1(char *host){
  CLIENT *clnt;
	void  *result_1;
	char * write_1_arg;

	showmessage_1_arg = (char *) malloc(sizeof(char)*100);
	bzero(showmessage_1_arg, 100);

	wclear(winInput);
	wrefresh(winInput);

	wprintw(winInput, "Missatge: ");
	wrefresh(winInput);

	wgetstr(winInput, write_1_arg);
	wrefresh(winInput);

	char * envia = (char *) malloc(sizeof(char)*150);
	bzero(envia, 150);

	sprintf(envia, "%s: %s", clientUser, write_1_arg);

  #ifndef	DEBUG
  	clnt = clnt_create ("localhost", PROGRAM, ZERO, "udp");
  	if (clnt == NULL) {
  		clnt_pcreateerror (host);
  		exit (1);
  	}
  #endif	/* DEBUG */

	result_1 = write_1(&envia, clnt);
	if (result_1 == (void *) NULL) {
		clnt_perror (clnt, "call failed");
	}

  #ifndef	DEBUG
  	clnt_destroy (clnt);
  #endif	 /* DEBUG */

	// EXEMPLES
	/*result_1 = getchat_1((void*)&getchat_1_arg, clnt);
	if (result_1 == (char **) NULL) {
		clnt_perror (clnt, "call failed");
	}
	result_2 = write_1(&write_1_arg, clnt);
	if (result_2 == (void *) NULL) {
		clnt_perror (clnt, "call failed");
	}*/
}

int main (int argc, char *argv[]){
	char *host;
  host = argv[1];

	if (argc < 2) {
		printf ("usage: %s server_host\n", argv[0]);
		exit (1);
	}
  // --- SIGNALS ---
  // Reprogramem el signal de SIGINT per tancar el programa
	signal(SIGINT, sortida);


  // --- NCURSES INIT ---
  // Inicialitzem el sistema de ncurses amb dues finestres
  initscr();
  // Finestra per escriure al chat.
  winInput = newwin(10, 40, 16, 0);
  // Finestra per mostrar el chat.
  winChat = newwin(15, 40, 0, 0);
  // Necesari per
  scrollok(winChat, TRUE);

  // --- MAIN FUNCTIONS ---
  // Inicialitzem un thread per lectura del chat del sistema.
  pthread_t thread;
  pthread_create(&thread, NULL, getChatThread, NULL);

  // Loop infinit de lectura del teclat
  while(1) {
    program_1(host);
  }

  exit (0);
}
