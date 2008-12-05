package set;
import javax.swing.JApplet;
import java.awt.GridLayout;
import java.awt.Container;
import idl.SetHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import java.util.LinkedList;
import java.util.List;

public class Applet extends JApplet {
	idl.Board board;
	private void doupdate() {
		Container cp = getContentPane();
		cp.removeAll();
		cp.setLayout(new GridLayout(board.getCols(),3));
		for (int col = 0; col < board.getCols(); col++) {
			for (int row = 0; row < 3; row++) {
				int shape = 0, count = 0, fill = 0, color = 0;
				int card = board.getCard(col,row);
				for (int i = 0; i < 12; i++) {
					if ((card & (1 << i)) > 0) {
						switch (i) {
							case 0:
								shape = CardComponent.WAVE;
								break;
							case 1:
								shape = CardComponent.RECT;
								break;
							case 2:
								shape = CardComponent.DIAMOND;
								break;
							case 3:
								count = 1;
								break;
							case 4:
								count = 2;
								break;
							case 5:
								count = 3;
								break;
							case 6:
								color = CardComponent.PURPLE;
								break;
							case 7:
								color = CardComponent.RED;
								break;
							case 8:
								color = CardComponent.GREEN;
								break;
							case 9:
								fill = CardComponent.NONE;
								break;
							case 10:
								fill = CardComponent.LINES;
								break;
							case 11:
								fill = CardComponent.OPAQUE;
								break;
						}
					}
				}
				cp.add(new CardComponent(shape, count, fill,
					color, new AppletCardComponentListener(col, row)));
			}
		}
		cp.validate();
	}
	public void init() {
		ORB orb = ORB.init(this, null);
		org.omg.CORBA.Object objRef = null;
		// get the root naming context
		try {
			objRef = orb.resolve_initial_references("NameService");
		} catch (InvalidName e) {
			e.printStackTrace();
		}
		// Use NamingContextExt instead of NamingContext. This is
		// part of the Interoperable naming Service.
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		// resolve the Object Reference in Naming
		org.omg.CORBA.Object obj = null;
		try {
			obj = ncRef.resolve(new NameComponent[] { new NameComponent("set", "uxx"), new NameComponent("Set", "Object")});
		} catch (NotFound e2) {
			e2.printStackTrace();
		} catch (CannotProceed e2) {
			e2.printStackTrace();
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e2){
			e2.printStackTrace();
		}
		idl.Set set = SetHelper.narrow(obj);
		System.out.println("Obtained a handle on server object: " + set);
		board = set.getBoard();

		idl.BoardObserverPOA observer = (new idl.BoardObserverPOA() {
			public void update() {
				doupdate();
			}
		});

		POA rootPOA = null;
		try {
		//Instantiate Servant and create reference
			rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
		} catch (InvalidName e) {
			e.printStackTrace();
		}
		try {
			rootPOA.activate_object(observer);
		} catch (ServantAlreadyActive e) {
			e.printStackTrace();
		} catch (WrongPolicy e) {
			e.printStackTrace();
		}
		POAManager pman = rootPOA.the_POAManager();
		try {
			pman.activate();
		} catch (AdapterInactive e) {
			e.printStackTrace();
		}
		board.addBoardObserver(observer._this());
		doupdate();
	}
	List selectedCard = new LinkedList();
	private class AppletCardComponentListener implements CardComponentListener {
		Integer pos;
		public boolean setSelected(boolean selected) {
			boolean ret = (!selected) && (selectedCard.size() < 3);
			if (ret) {
				selectedCard.add(pos);
			} else {
				selectedCard.remove(pos);
			}
			if (selectedCard.size() == 3) {
				if (board.removeSet(
					((Integer)selectedCard.get(0)).intValue()/3,
					((Integer)selectedCard.get(0)).intValue()%3,
					((Integer)selectedCard.get(1)).intValue()/3,
					((Integer)selectedCard.get(1)).intValue()%3,
					((Integer)selectedCard.get(2)).intValue()/3,
					((Integer)selectedCard.get(2)).intValue()%3
				)) {

					System.out.println("Set!");
					selectedCard.clear();
				}
			}
			return ret;
		}
		public AppletCardComponentListener(int col, int row) {
			pos = new Integer((col*3)+row);
		}
	}
}
