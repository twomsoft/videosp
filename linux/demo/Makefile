
TARGET = demo

all: $(TARGET)

.SUFFIXES:
.PHONY: all clean

CFLAGS = -O2 -DNDEBUG -std=c++11

INCLUDES = `pkg-config --cflags gtk+-2.0`

%.o:%.cpp
	@echo g++ $< ; g++ -c $< -o $@ $(CFLAGS) $(INCLUDES) 
	
OBJS = main.o 

LINKS = -lpthread -lm `pkg-config --libs gtk+-2.0` \
	-lX11 -L/usr/X11R6/lib \
	-lfastplay -L../bin \
	-Wl,-rpath=.
	
$(TARGET): $(OBJS)
	g++ -o $@ $(OBJS) $(LINKS)
	cp $@ ../bin

clean:
	rm $(TARGET) $(OBJS)
