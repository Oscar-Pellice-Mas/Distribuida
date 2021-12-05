struct Message{
	char * origin;
	char * text;
};

struct ChatLog{
	int num_messages;
	Message * chat;
};

program PROGRAM {
 version VERSION {
   ChatLog getChat(void) = 1;
   void write(string) = 2;
 } = 1;
} = 0x20000001;
