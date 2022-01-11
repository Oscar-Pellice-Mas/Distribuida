/*
 * This is sample code generated by rpcgen.
 * These are only templates and you can use them
 * as a guideline for developing your own functions.
 */

#include "S4.h"

#define FILE_ERROR "Error en obrir el fitxer"

char* readUntil(int fd, char cFi) {
    int i = 0;
    char c = '0';
    char* buffer = (char*)malloc(sizeof(char));
		int bytes;

    while (c != cFi) {
        bytes= read(fd, &c, sizeof(char));
				if (bytes==0) {
					buffer[i] = '\0';
					return buffer;
				}
        if (c != cFi) {
            buffer[i] = c;
            buffer = (char*)realloc(buffer, sizeof(char) * (i + 2));
        }
        i++;
    }
    buffer[i - 1] = '\0';
    return buffer;
}

void readFile(int fd, ChatLog * log){
	int i=0;

	//Reservem espai
	log->chat = malloc(sizeof(Message));
	//Llegim l'arxiu
	while (1) {
		log->chat[i].origin = readUntil(fd,'*');
		log->chat[i].text = readUntil(fd,'\n');
		//Comprovem si fi d'arxiu
		if (strlen(log->chat[i].origin)<=1) {
			//Guarden el num de missatges
			log->num_messages = i;
			break;
		}else{
			//Si no hem acabat, guardem i reservem memoria
			(*log).chat = realloc((Message*)log->chat, sizeof(Message)*(i+2));
			i++;
		}
	}
}

void writeFile_line(int fd,char * newMSG){
	write(fd, newMSG, strlen(newMSG));
}

ChatLog *
getchat_1_svc(void *argp, struct svc_req *rqstp)
{
	static ChatLog  result;
	int fd;
	//Obrim l'arxiu de log
	fd = open ("chat.txt", O_RDONLY|O_CREAT);
	if (fd<0){
	write(1,FILE_ERROR,sizeof(FILE_ERROR));
	return &result;
	}

	readFile(fd, &result);

	return &result;
}

/*Formato de cadenas a enviar: Como en el txt (<origen>*<texto>)*/
void *
write_1_svc(char **argp, struct svc_req *rqstp)
{
	static char * result;
	ChatLog  buffer;
	fd = open ("chat.txt", O_RDWR|O_CREAT, 0666);
	if (fd<0){
		printf("Error en obrir el fitxer");
		return;
	}
	// ESCRIURE AL FITXER LA VARIABLE DE ARGP
	readFile(fd, &buffer);
	writeFile_line(fd,*argp);
	close(fd);
	return (void *) &result;
}
