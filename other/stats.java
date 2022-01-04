public void updateGlobalStats(ArrayList<Integer> stats) {
        avgOrders = (avgOrders + stats.get(0)) / counter;
        avgKm = (avgKm + stats.get(1)) / counter;
        avgBattery = (avgBattery + stats.get(2)) / counter;
        counter++;

        System.out.println("Global stats: " + avgOrders + " - " + avgKm + " - " + avgBattery);
        }

//Global stats
private int counter;
private float avgOrders;
private float avgKm;
private float avgPollution;
private float avgBattery;



        TopologyDrone td = d.getNetworkTopology().getDroneWithId(request.getId());
        td.setPosition(new Coordinate(request.getX(), request.getY()));

        ArrayList<Integer> stats = new ArrayList<>();
        stats.add(request.getNOrders());
        stats.add(request.getDistance());
        stats.add(request.getBattery());
        d.updateGlobalStats(stats);

        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
        .setResp("new pos...")
        .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();