package com.motsuni.globalstorage.itemstack;

public class NavigatorManager {
    protected NavigatorPagerPrevious previous;
    protected NavigatorPagerNext next;

    public NavigatorManager() {
        this.previous = new NavigatorPagerPrevious();
        this.next = new NavigatorPagerNext();
    }

    public NavigatorPagerPrevious getPrevious() {
        return this.previous;
    }

    public NavigatorPagerNext getNext() {
        return this.next;
    }
}
