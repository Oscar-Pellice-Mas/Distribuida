/*
 * This is sample code generated by rpcgen.
 * These are only templates and you can use them
 * as a guideline for developing your own functions.
 */

#include "S4.h"

char* name;
int end = 0;

char* readLine(int fd) {
    int i = 0;
    char c = '0';
    char* buffer = (char*)malloc(sizeof(char)*strlen(name)+2);
		int bytes;

		strcat(buffer, name);

    while (c != '\n') {
        bytes= getch();
				if (bytes==0) {
					buffer[i] = '\0';
					return buffer;
				}
        if (c != '\n') {
            buffer[i] = c;
            buffer = (char*)realloc(buffer, sizeof(char) * (i + 2));
        }
        i++;
    }
    buffer[i - 1] = '\0';
    return buffer;
}

void *refreshChat()
{
	char * write_1_arg;
	int ch;
	// ACTIVAR THREAD PER LLEGIR TECLAT
	// I ENVIAR RPC AMB EL write_1
	while (true) {
		write_1_arg = readLine();

		if (strcmp(write_1_arg,"EXIT")) {
			end = 1;
			exit();
		}

		result_2 = write_1(&write_1_arg, clnt);
		if (result_2 == (void *) NULL) {
			clnt_perror (clnt, "call failed");
		}
	}
}

void
program_1(char *host)
{
	CLIENT *clnt;
	ChatLog  *result_1;
	char *getchat_1_arg;
	void  *result_2;

#ifndef	DEBUG
	clnt = clnt_create (host, PROGRAM, VERSION, "udp");
	if (clnt == NULL) {
		clnt_pcreateerror (host);
		exit (1);
	}
#endif	/* DEBUG */

	// INIT DE NCURSES
	initscr();
	char* buffer = (char*)malloc(sizeof(char)*strlen("anonymous"));
	strcat(buffer, "anonymous");
	// FER THREADS
	pthread_t thread_id;

  pthread_create(&thread_id, NULL, refreshChat, NULL);

	// FER getchat_1 CADA SEGON
	// PRINTAR PER PANTALLA I FER REFRESH
	while(end == 0){
		sleep(1);
		result_1 = getchat_1((void*)&getchat_1_arg, clnt);
		if (result_1 == (char **) NULL) {
			clnt_perror (clnt, "call failed");
		}
		printw(result_1);
		refresh();
	}
	pthread_join(thread_id, NULL);

	// EXEMPLES
	/*result_1 = getchat_1((void*)&getchat_1_arg, clnt);
	if (result_1 == (char **) NULL) {
		clnt_perror (clnt, "call failed");
	}
	result_2 = write_1(&write_1_arg, clnt);
	if (result_2 == (void *) NULL) {
		clnt_perror (clnt, "call failed");
	}*/

	endwin();

#ifndef	DEBUG
	clnt_destroy (clnt);
#endif	 /* DEBUG */
}


int
main (int argc, char *argv[])
{
	char *host;

	if (argc < 2) {
		printf ("usage: %s server_host\n", argv[0]);
		exit (1);
	}
	host = argv[1];
	program_1 (host);
exit (0);
}
