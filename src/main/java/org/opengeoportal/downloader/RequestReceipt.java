package org.opengeoportal.downloader;

/**
 * Created by cbarne02 on 3/11/16.
 */
public class RequestReceipt {

    public RequestReceipt(){}

    public RequestReceipt(String ticket){
        this.ticket = ticket;
    }

    private String ticket;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}
