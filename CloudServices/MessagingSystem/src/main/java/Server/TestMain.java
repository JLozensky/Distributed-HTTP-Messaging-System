package Server;

public class TestMain {

    public static void main(String[] args) {

        //test GetSkierTotalVertDay
        int vert = DatabaseReader.getSkierTotalVertDay("456","123","2021","34");
        System.out.println("this is total vert " + vert);

        int wrongSkier = DatabaseReader.getSkierTotalVertDay("46","123","2021","34");
        System.out.println("this is wrong skier " + wrongSkier);
        int wrongResort = DatabaseReader.getSkierTotalVertDay("456","13","2021","34");
        System.out.println("this is wrong resort " + wrongResort);
        int wrongSeason = DatabaseReader.getSkierTotalVertDay("456","123","2020","34");
        System.out.println("this is wrong season " + wrongSeason);
        int wrongDay = DatabaseReader.getSkierTotalVertDay("456","123","2021","35");
        System.out.println("this is wrong day " + wrongDay);

        //test GetSkierTotalVert
        int vertTotal = DatabaseReader.getSkierTotalVert("456","123");
        System.out.println("this is total vert " + vertTotal);

        int vert2018 = DatabaseReader.getSkierTotalVert("456","123","2018");
        System.out.println("this is total vert 2018 " + vert2018);


        int wrongSkierVertTotal = DatabaseReader.getSkierTotalVert("46","123","2021");
        System.out.println("this is wrong skier " + wrongSkierVertTotal);
        int wrongResortVertTotal = DatabaseReader.getSkierTotalVert("456","13","2021");
        System.out.println("this is wrong resort " + wrongResortVertTotal);
        int wrongSeasonVertTotal = DatabaseReader.getSkierTotalVert("456","123","2020");
        System.out.println("this is wrong season " + wrongSeasonVertTotal);

        int skiers = DatabaseReader.getResortUniqueSkierDay("123","2021", "34");
        System.out.println("this is total skiers 2021 " + skiers);

        int skiers2018 = DatabaseReader.getResortUniqueSkierDay("123","2018", "34");
        System.out.println("this is total skiers 2018 " + skiers2018);


        int wrongDaySkiers = DatabaseReader.getResortUniqueSkierDay("123","2021", "33");
        System.out.println("this is wrong day " + wrongDaySkiers);
        int wrongResortSkiers = DatabaseReader.getResortUniqueSkierDay("13","2021", "34");
        System.out.println("this is wrong resort " + wrongResortSkiers);
        int wrongSeasonSkiers = DatabaseReader.getResortUniqueSkierDay("123","2020", "34");
        System.out.println("this is wrong season " + wrongSeasonSkiers);

    }

}
