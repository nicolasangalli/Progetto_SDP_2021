package ServerAmministratore;

import Libraries.Coordinate;
import Libraries.GlobalStat;
import Libraries.TopologyDrone;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


@XmlRootElement
public class SmartCity {

    private static SmartCity instance;
    private ArrayList<TopologyDrone> drones;
    private ArrayList<GlobalStat> globalStats;

    private SmartCity() {
        drones = new ArrayList<>();
        globalStats = new ArrayList<>();
    }

    public synchronized static SmartCity getInstance() {
        if(instance == null) {
            instance = new SmartCity();
        }
        return instance;
    }

    public synchronized boolean checkAvailable(int id) {
        for(TopologyDrone d : drones) {
            if(d.getId() == id) {
                return false;
            }
        }
        return true;
    }

    public synchronized Coordinate addNewDrone(TopologyDrone d) {
        //generate random position
        Random random = new Random();
        Coordinate position = new Coordinate(random.nextInt(10), random.nextInt(10));

        d.setPosition(position);
        drones.add(d);

        return position;
    }

    //return a list with the position of drone and all the other drones
    public ArrayList<Object> getDrones(int id, int x, int y) {
        ArrayList<TopologyDrone> list = drones;
        ArrayList<Object> ret = new ArrayList<>();

        ret.add(x);
        ret.add(y);
        for(TopologyDrone d : list) {
            if(d.getId() != id) {
                ret.add(d);
            }
        }

        return ret;
    }

    public synchronized void removeDrone(int id) {
        for(TopologyDrone d : drones) {
            if(d.getId() == id) {
                drones.remove(d);
                break;
            }
        }
    }

    //return the drones list
    public ArrayList<TopologyDrone> getDronesList() {
        ArrayList<TopologyDrone> list = drones;
        return list;
    }

    public synchronized void addStat(GlobalStat globalStat) {
        globalStats.add(globalStat);
    }

    public ArrayList<GlobalStat> getGlobalStats(int n) {
        ArrayList<GlobalStat> list = (ArrayList<GlobalStat>) globalStats.clone();
        if(n >= list.size() || n <= 0) {
            Collections.reverse(list);
            return list;
        } else {
            ArrayList<GlobalStat> shortList = new ArrayList<>();
            for(int i=1; i<=n; i++) {
                GlobalStat globalStat = list.get(list.size()-i);
                shortList.add(globalStat);
            }
            return shortList;
        }
    }

    public double getAvgDeliveries(String[] timestamps) {
        ArrayList<GlobalStat> list = (ArrayList<GlobalStat>) globalStats.clone();
        double avg = 0;
        int counter = 0;

        Timestamp ts1;
        Timestamp ts2;

        try {
            ts1 = Timestamp.valueOf(timestamps[0]);
            ts2 = Timestamp.valueOf(timestamps[1]);
        } catch (IllegalArgumentException iae) {
            return  -1;
        }

        if(ts1.after(ts2)) {
            return -1;
        }

        for(GlobalStat gs : list) {
            Timestamp ts = Timestamp.valueOf(gs.getTimestamp());
            if((ts.after(ts1) || ts == ts1) && (ts.before(ts2) || ts == ts2)) {
                avg += gs.getAvgOrder();
                counter++;
            }
            if(ts.after(ts2)) {
                break;
            }
        }
        if(counter != 0) {
            avg /= counter;
        }
        return Math.round(avg*100.0)/100.0;
    }

    public double getAvgDistances(String[] timestamps) {
        ArrayList<GlobalStat> list = (ArrayList<GlobalStat>) globalStats.clone();
        double avg = 0;
        int counter = 0;

        Timestamp ts1;
        Timestamp ts2;

        try {
            ts1 = Timestamp.valueOf(timestamps[0]);
            ts2 = Timestamp.valueOf(timestamps[1]);
        } catch (IllegalArgumentException iae) {
            return  -1;
        }

        if(ts1.after(ts2)) {
            return -1;
        }

        for(GlobalStat gs : list) {
            Timestamp ts = Timestamp.valueOf(gs.getTimestamp());
            if((ts.after(ts1) || ts == ts1) && (ts.before(ts2) || ts == ts2)) {
                avg += gs.getAvgKm();
                counter++;
            }
            if(ts.after(ts2)) {
                break;
            }
        }
        if(counter != 0) {
            avg /= counter;
        }
        return Math.round(avg*100.0)/100.0;
    }

}
