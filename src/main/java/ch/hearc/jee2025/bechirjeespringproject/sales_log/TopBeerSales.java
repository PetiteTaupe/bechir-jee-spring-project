package ch.hearc.jee2025.bechirjeespringproject.sales_log;

public class TopBeerSales {

    public TopBeerSales(Long beerId, String beerName, long totalQuantity) {
        this.beerId = beerId;
        this.beerName = beerName;
        this.totalQuantity = totalQuantity;
    }

    public Long getBeerId() {
        return beerId;
    }

    public String getBeerName() {
        return beerName;
    }

    public long getTotalQuantity() {
        return totalQuantity;
    }

    private final Long beerId;
    private final String beerName;
    private final long totalQuantity;
}