package it.sc.model;

public class Content {

	private String id;
	private String type;
	private String slug;
	private String seasons_count;
	private String title;
	private String date;
	private String episode;
	private String fileNameTmp;

	public Content() {}
 

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSlug() {
		return slug;
	}
	public void setSlug(String slug) {
		this.slug = slug;
	}
	public String getSeasons_count() {
		return seasons_count;
	}
	public void setSeasons_count(String seasons_count) {
		this.seasons_count = seasons_count;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getEpisode() {
		return episode;
	}
	public void setEpisode(String episode) {
		this.episode = episode;
	}
	public String getFileNameTmp() {
		return fileNameTmp;
	}
	public void setFileNameTmp(String fileNameTmp) {
		this.fileNameTmp = fileNameTmp;
	}

	@Override
	public String toString() {
		return "Content [id=" + id + ", type=" + type + ", slug=" + slug + ", seasons_count=" + seasons_count
				+ ", title=" + title + ", date=" + date + ", episode=" + episode + ", fileNameTmp=" + fileNameTmp + "]";
	}
}
