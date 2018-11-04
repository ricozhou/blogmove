package blogmove.utils;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ddf.EscherColorRef.SysIndexProcedure;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import blogmove.config.FilePathConfig;
import blogmove.constant.FileOtherConstant;
import blogmove.domain.Blogcontent;
import blogmove.domain.Blogmove;

/**
 * @author ricozhou
 * @date Oct 17, 2018 12:51:52 PM
 * @Desc
 */
public class BlogMoveTouTiaoUtils {

	/**
	 * @date Oct 31, 2018 3:59:49 PM
	 * @Desc 获取文章list请求url
	 * @param blogMove
	 * @param num
	 * @param max_behot_time
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws FailingHttpStatusCodeException
	 */
	public static String getTouTiaoListUrl(Blogmove blogMove, int num, String max_behot_time)
			throws Exception {
		String oneUrl = "https://www.toutiao.com/c/user/article/?page_type=1&user_id=%s&max_behot_time=%s&count=20&as=%s&cp=%s&_signature=%s";
		String user_id = blogMove.getMoveUserId();
//		System.out.println(user_id);
		String as = "";
		String cp = "";
		String _signature = "";
		//更改文件
		updateHtmlFile("C:/Users/rzhou6/Desktop/toutiao/newd.html",user_id,max_behot_time);
		
		String urlOne = "file:///C:/Users/rzhou6/Desktop/toutiao/newd.html";
		// 模拟浏览器操作
		// 创建WebClient
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		// 关闭css代码功能
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setCssEnabled(false);
		// 如若有可能找不到文件js则加上这句代码
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

		HtmlPage page2 = webClient.getPage(urlOne);
		 System.out.println(page2.asText());
		// 执行js

		as = page2.getElementById("as").asText();
		cp = page2.getElementById("cp").asText();
		_signature = page2.getElementById("_signature").asText();
		System.out.println(_signature);
		_signature = getRightSign(_signature);
		
		System.out.println(as);
		System.out.println(cp);
		System.out.println(_signature);
		oneUrl = String.format(oneUrl, user_id, max_behot_time, as, cp, _signature);
		System.out.println(oneUrl);
		return oneUrl;
	}

	/**
	 * @date Nov 2, 2018 12:36:27 PM
	 * @Desc
	 * @param string
	 * @param max_behot_time 
	 * @param user_id 
	 */
	private static void updateHtmlFile(String string, String user_id, String max_behot_time) {
		String html=FileUtils.getFileToString(string);
		Document doc = Jsoup.parse(html);
		Element imgTags = doc.getElementById("user_id");
		imgTags.attr("value",user_id);
		Element imgTags2 = doc.getElementById("max_behot_time");
		imgTags2.attr("value",max_behot_time);
		
		//写入文件
		new File(string).delete();
		FileUtils.appendFile(string, doc.html());
		
	}

	/**
	 * @date Nov 2, 2018 12:24:42 PM
	 * @Desc
	 * @param _signature
	 * @return
	 */
	private static String getRightSign(String _signature) {
		// w:8,x:9,y:-,z:.
		// >AAAAAAAAAA BXN0hbnC y jAQ AAAB
		// >AAAAAAAAAA AYyN2oFg - jAQ AAAB
		String s = _signature.substring(18, 19);
		String ss = _signature.substring(19, 22);
		if ("8".equals(s)) {
			s = "w";
		} else if ("9".equals(s)) {
			s = "x";
		} else if ("-".equals(s)) {
			s = "y";
		} else if (".".equals(s)) {
			s = "z";
		}

		return "AAAAAAAAAABXN0hbnC" + s + ss + "AAAB";
	}

	/**
	 * @date Oct 31, 2018 3:59:49 PM
	 * @Desc 获取文章list请求url
	 * @param blogMove
	 * @param num
	 * @param max_behot_time
	 * @return
	 */
	public static String getTouTiaoArticleMsgJsonString(Elements pageMsg22) {
		String value = null;

		try {
			// 正文
			/* 循环遍历script下面的JS变量 */
			for (Element element : pageMsg22) {
				/* 取得JS变量数组 */
				String[] data = element.data().toString().split("var");
				/* 取得单个JS变量 */
				for (String variable : data) {

					/* 过滤variable为空的数据 */
					if (variable.contains("=") && variable.trim().startsWith("BASE_DATA")) {
						/* 取到满足条件的JS变量 */
						if (variable.contains("articleInfo") && variable.contains("content")) {
							value = variable.trim().startsWith("BASE_DATA =") ? (variable.trim().substring(11))
									: variable.trim();
							value = value.endsWith("//]]>") ? (value.substring(0, value.length() - 8)) : value;
							value = value.replace(".replace(/<br \\/>/ig, '')", "");

							// System.out.println(value);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	// /**
	// * @date Oct 17, 2018 1:10:19 PM
	// * @Desc 获取标题
	// * @param doc
	// * @return
	// */
	// public static String getTouTiaoArticleTitle(Document doc) {
	// // 标题
	// Element pageMsg2 =
	// doc.select("div.article-detail").first().select("h1.header").first();
	// return pageMsg2.ownText();
	// }
	//
	// /**
	// * @date Oct 17, 2018 1:10:28 PM
	// * @Desc 获取作者
	// * @param doc
	// * @return
	// */
	// public static String getTouTiaoArticleAuthor(Document doc) {
	// Element pageMsg2 =
	// doc.select("div.article-detail").first().select("a.__user").first().select("span").first();
	// return pageMsg2.html();
	// }
	//
	// /**
	// * @date Oct 17, 2018 1:10:33 PM
	// * @Desc 获取时间
	// * @param doc
	// * @return
	// */
	// public static Date getTouTiaoArticleTime(Document doc) {
	// Element pageMsg2 =
	// doc.select("div.article-detail").first().select("div.item").first();
	// String date = pageMsg2.ownText().trim();
	// if (date.startsWith("发布于")) {
	// date = date.substring(date.indexOf("发布于") + 3).trim();
	// }
	// if (date.indexOf(CommonSymbolicConstant.FORWARD_SLASH) < 4) {
	// date = DateUtils.format(new Date(), DateUtils.YYYY) +
	// CommonSymbolicConstant.FORWARD_SLASH + date;
	// }
	// // 这地方时间格式变化太多暂时不实现
	// Date d = DateUtils.formatStringDate(date,
	// DateUtils.YYYY_MM_DD_HH_MM_SS3);
	// // 注意有些格式不正确
	// return d == null ? new Date() : d;
	// }
	//
	// /**
	// * @date Oct 17, 2018 1:10:37 PM
	// * @Desc 获取类型
	// * @param doc
	// * @return
	// */
	// public static String getTouTiaoArticleType(Document doc) {
	// Element pageMsg2 =
	// doc.select("div.article-detail").first().select("h1.header").first().select("div.horizontal")
	// .first();
	// if ("原".equals(pageMsg2.html())) {
	// return "原创";
	// } else if ("转".equals(pageMsg2.html())) {
	// return "转载";
	// } else if ("译".equals(pageMsg2.html())) {
	// return "翻译";
	// }
	// return "原创";
	// }

	/**
	 * @date Oct 17, 2018 1:10:41 PM
	 * @Desc 获取正文
	 * @param content
	 * @param object
	 * @param blogcontent
	 * @return
	 */
	public static String getTouTiaoArticleContent(String content, Blogmove blogMove, Blogcontent blogcontent) {
		String images;
		// 注意是否需要替换图片
		if (blogMove.getMoveSaveImg() == 0) {
			// 保存图片到本地
			// 先获取所有图片连接，再按照每个链接下载图片，最后替换原有链接
			// 先创建一个文件夹
			// 先创建一个临时文件夹
			String blogFileName = String.valueOf(UUID.randomUUID());
			FileUtils.createFolder(FilePathConfig.getUploadBlogPath() + File.separator + blogFileName);
			blogcontent.setBlogFileName(blogFileName);
			// 匹配出所有链接
			List<String> imgList = BlogMoveCommonUtils.getArticleImgList(content);
			// 下载并返回重新生成的imgurllist
			List<String> newImgList = getTouTiaoArticleNewImgList(blogMove, imgList, blogFileName);
			// 拼接文章所有链接
			images = BlogMoveCommonUtils.getArticleImages(newImgList);
			blogcontent.setImages(images);
			// 替换所有链接按顺序
			content = getTouTiaoNewArticleContent(content, imgList, newImgList);

		}

		return content;
	}

	/**
	 * @date Oct 22, 2018 3:31:40 PM
	 * @Desc
	 * @param content
	 * @param imgList
	 * @param newImgList
	 * @return
	 */
	private static String getTouTiaoNewArticleContent(String content, List<String> imgList, List<String> newImgList) {
		Document doc = Jsoup.parse(content);
		Elements imgTags = doc.select("img[src]");
		if (imgList == null || imgList.size() < 1 || newImgList == null || newImgList.size() < 1 || imgTags == null
				|| "".equals(imgTags)) {
			return content;
		}
		for (int i = 0; i < imgTags.size(); i++) {
			imgTags.get(i).attr("src", newImgList.get(i));
		}
		return doc.body().toString();
	}

	/**
	 * @date Oct 22, 2018 3:31:33 PM
	 * @Desc
	 * @param imgList
	 * @return
	 */
	private static List<String> getTouTiaoArticleNewImgList(Blogmove blogMove, List<String> imgList,
			String blogFileName) {
		// 下载图片
		if (imgList == null || imgList.size() < 1) {
			return null;
		}
		List<String> newImgList = new ArrayList<String>();
		String uuid;
		String filePath = FilePathConfig.getUploadBlogPath() + File.separator + blogFileName;
		String fileName;
		for (String url : imgList) {
			uuid = String.valueOf(UUID.randomUUID());
			fileName = BlogMoveCommonUtils.downloadImg(url, uuid, filePath, blogMove);
			// 打水印
			if (blogMove.getMoveAddWaterMark() == 0) {
				BlogMoveCommonUtils.addImgWaterMark(blogFileName, fileName, blogMove);
			}

			newImgList.add(FileOtherConstant.FILE_JUMP_PATH_PREFIX3 + blogFileName + File.separator + fileName);
		}

		return newImgList;
	}

}
