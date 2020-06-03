public class Page {
    public String url,content,title;
    public Double dateScore,geographicScroe;
    public Page(String url,String content,String title,Double dateScore,Double geographicScore){
        this.url=url;
        this.content=content;
        this.title=title;
        this.dateScore=dateScore;
        this.geographicScroe=geographicScore;
    }
}