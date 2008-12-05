#include "set.hh"
#include <iostream>
#include <vector>
#include <iterator>

using namespace PortableServer;
using namespace std;

/* -------------------------------------------------------------------------- */
class Board_i : public POA_idl::Board {
	private:
		vector<idl::BoardObserver_ptr> observers;
		// int: shape(2)-count(2)-fill(2)-color(2)
		vector<CORBA::Long> board[3];
		bool checkSet(int x1, int y1, int x2, int y2, int x3, int y3);
		bool setPossible();
		void addCards(int count);
	public:
		virtual void update();
		virtual CORBA::Long getCols();
		virtual CORBA::Long getCard(CORBA::Long col, CORBA::Long row);
		virtual void addBoardObserver(idl::BoardObserver_ptr myboardobserver);
		virtual CORBA::Boolean removeSet(
			CORBA::Long x1, CORBA::Long y1,
			CORBA::Long x2, CORBA::Long y2,
			CORBA::Long x3, CORBA::Long y3
		);
		Board_i();
};

//void* run_update(void * arg)  {
//	Board_i *board = (Board_i*)arg;
//	cout << "run_update: " << board << endl;
//	board->update();
//	return NULL;
//}

Board_i::Board_i() {
	srand(time(NULL));
	while (!setPossible()) {
		addCards(3);
	}
}

void Board_i::addCards(int count) {
	count += count%3;
	// first fill gaps
	for (unsigned int row = 0; row < 3; row++) {
		for (unsigned int col = 0; col < board[row].size(); col++) {
			if (board[row][col] == 0) {
				// -------------- -------------- -------------- ---------------
				//|2048 1024  512| 256  128   64|  32   16    8|    4    2    1
				board[row][col] = (
					(1   << (rand()%3)) |
					(8   << (rand()%3)) |
					(64  << (rand()%3)) |
					(512 << (rand()%3))
				);
				//cout <<
				//	((1   << (shape)) |
				//	(8   << (count)) |
				//	(64  << (color)) |
				//	(512 << (fill))) << endl;
				count--;
				if (count <= 0) {
						return;
				}
			}
		}
	}
	// then append new cols
	int row = 0;
	while (count > 0) {
		cout << "count:" << count << " row:" << row << endl;
		board[row].push_back(
			(1   << (rand()%3)) |
			(8   << (rand()%3)) |
			(64  << (rand()%3)) |
			(512 << (rand()%3))
		);
		count--;
		row++;
		if (row >= 3) row = 0;
	}
}

CORBA::Long Board_i::getCols() {
	return board[0].size();
}
CORBA::Long Board_i::getCard(CORBA::Long col, CORBA::Long row) {
	return board[row][col];
}
void Board_i::addBoardObserver(idl::BoardObserver_ptr myboardobserver) {
	cout << "adding boardobserver (" << myboardobserver << ")" << endl;
	observers.push_back(idl::BoardObserver::_duplicate(myboardobserver));
}
void Board_i::update() {
	for (vector<idl::BoardObserver_ptr>::iterator iter = observers.begin(); iter != observers.end(); iter++) {
		idl::BoardObserver_ptr val = *iter;
		cout << "calling " << val << "->update()" << endl;
		val->update();
		cout << "ok" << endl;
	}
}
CORBA::Boolean Board_i::removeSet(CORBA::Long x1, CORBA::Long y1, CORBA::Long x2, CORBA::Long y2, CORBA::Long x3, CORBA::Long y3) {
	bool set = checkSet(x1,y1,x2,y2,x3,y3);
	if (set) {
		board[y1][x1] = 0;
		board[y2][x2] = 0;
		board[y3][x3] = 0;
		while (!setPossible()) {
			addCards(3);
		}
		update();
	}
	return set;
}

bool Board_i::checkSet(int x1, int y1, int x2, int y2, int x3, int y3) {
	//  The Magic Rule
	//  If two are... and one is not, then it is not a 'Set'.
	cout << ((x1*3)+y1) << "," <<
		((x2*3)+y2) << "," <<
		((x3*3)+y3) << endl;
	int sum[3];
	sum[0] = (getCard(x1,y1) & getCard(x2,y2));
	sum[1] = (getCard(x1,y1) & getCard(x3,y3));
	sum[2] = (getCard(x2,y2) & getCard(x3,y3));
	if ((sum[0] == 0) || (sum[1] == 0) || (sum[2] == 0)) return false;
	for (int attr = 0; attr < 12; attr+=3) {
		int cardscount = 0;
		for (int cards = 0; cards < 3; cards++) {
			if (((7 << attr) & sum[cards]) >> attr) {
				cardscount++;
				cout << "checkSet: attr:" << attr << " cards:" << cards << endl;
			}
		}
		if ((cardscount == 2) || (cardscount == 1)) return false;
	}
	return true;
}

bool Board_i::setPossible() {
	int length = getCols()*3;
	for (int z = 0; z < (length-2); z++) {
		for (int y = z+1; y < (length-1); y++) {
			for (int x = y+1; x < (length); x++) {
				if (checkSet(z/3, z%3,
					y/3, y%3,
					x/3, x%3)) {
					cout << z << "," << y << "," << x << endl;
					return true;
				}
			}
		}
	}
	return false;
}


/* -------------------------------------------------------------------------- */

/* -------------------------------------------------------------------------- */
class Set_i : public POA_idl::Set {
	private:
		Board_i board;
	public:
		virtual idl::Board_ptr getBoard();
};

idl::Board_ptr Set_i::getBoard() {
	cout << "hallo daar" << endl;
	return board._this();
}
/* -------------------------------------------------------------------------- */

namespace {
	CORBA::ORB_var orb;
}

int main(int argc, char** argv, char**env) {
	// Initialise the ORB.
	orb = CORBA::ORB_init(argc, argv);

	// Obtain a reference to the root POA.
	CORBA::Object_var obj = orb->resolve_initial_references("RootPOA");
	POA_var poa = POA::_narrow(obj);

	Set_i set;
	// Activate the object.  This tells the POA that this object is
	// ready to accept requests.
	PortableServer::ObjectId_var myechoid = poa->activate_object(&set);

	// Obtain a reference to the object.
	idl::Set_var setref = set._this();

	// Lookup the naming service
	CORBA::Object_var initServ = orb->resolve_initial_references("NameService");

	// narrow() the naming service reference into an actual NamingService.
	CosNaming::NamingContext_var rootContext = CosNaming::NamingContext::_narrow(initServ);

	// Bind a context called "set" to the root context:
	CosNaming::Name contextName;
	contextName.length(1);
	contextName[0].id   = (const char*) "set";       // string copied
	contextName[0].kind = (const char*) "uxx"; // string copied
	CosNaming::NamingContext_var testContext = rootContext->bind_new_context(contextName);

	// Bind objref with name Echo to the testContext:
	CosNaming::Name objectName;
	objectName.length(1);
	objectName[0].id   = (const char*) "Set";   // string copied
	objectName[0].kind = (const char*) "Object"; // string copied


	testContext->bind(objectName, setref);

	// Decrement the reference count of the object implementation, so
	// that it will be properly cleaned up when the POA has determined
	// that it is no longer needed.

	set._remove_ref();

	// Obtain a POAManager, and tell the POA to start accepting
	// requests on its objects.

	PortableServer::POAManager_var pman = poa->the_POAManager();
	pman->activate();

	orb->run();

	// Clean up all the resources.
	orb->destroy();
	return 0;
}
