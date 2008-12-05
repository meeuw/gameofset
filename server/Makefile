SHELL = /bin/sh
.SUFFIXES: .cpp .o

CXXFLAGS=-O3 -Wall
LIBS=-lomniORB4
CXX=g++

all : server

.cpp.o:
	$(CXX) -c $(CXXFLAGS) $< -o $@

.cc.o:
	$(CXX) -c $(CXXFLAGS) $< -o $@

set.hh setSK.cc: ../idl/set.idl
	omniidl -bcxx $<

server : setSK.o server.o
	$(CXX) $(LIBS) setSK.o server.o -lpthread -o $@

clean :
	rm -f *.o server set.hh setSK.cc
