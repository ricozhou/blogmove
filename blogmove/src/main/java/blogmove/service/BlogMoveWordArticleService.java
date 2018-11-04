package blogmove.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.jetty.websocket.api.WebSocketConstants;
import org.xml.sax.SAXException;

import blogmove.config.FilePathConfig;
import blogmove.constant.CommonSymbolicConstant;
import blogmove.domain.Blogcontent;
import blogmove.domain.Blogmove;
import blogmove.utils.BlogMoveCommonUtils;
import blogmove.utils.BlogMoveWordUtils;
import blogmove.utils.DateUtils;
import blogmove.utils.FileUtils;

/**
 * @author ricozhou
 * @date Oct 29, 2018 5:06:05 PM
 * @Desc
 */
public class BlogMoveWordArticleService {

	/**
	 * @date Oct 29, 2018 5:07:15 PM
	 * @Desc
	 * @param blogMove
	 * @param string
	 * @param string2
	 * @param bList
	 * @return
	 * @throws SAXException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static Blogcontent getWordArticleMsg(Blogmove blogMove, String fileName, String fileOName,
			List<Blogcontent> bList)
			throws IOException, ParserConfigurationException, TransformerException, SAXException {
		Blogcontent blogcontent = new Blogcontent();
		blogcontent.setArticleSource("WORD");

		// 获取标题
		// 文章标题即word文件名去掉后缀
		String title = FileUtils.getFileNameBeforePoint(fileOName);
		// 是否重复去掉
		if (blogMove.getMoveRemoveRepeat() == 0) {
			// 判断是否重复
			if (BlogMoveCommonUtils.articleRepeat(bList, title)) {
				return null;
			}
		}
		blogcontent.setTitle(title);
		// 获取作者
		blogcontent.setAuthor(blogMove.getBlogAuthor());
		// 获取时间
		blogcontent.setGtmCreate(new Date());
		blogcontent.setGtmModified(blogcontent.getGtmCreate());
		// 获取类型
		blogcontent.setType("原创");
		// 获取正文
		// 按照路径解析word并返回html字符串
		blogcontent.setContent(BlogMoveWordUtils.getWordArticleContent(fileName, blogMove, blogcontent));

		// 是否打水印
		// 打水印
		if (blogMove.getMoveAddWaterMark() == 0) {
			String[] fileNames = new File(
					FilePathConfig.getUploadBlogPath() + File.separator + blogcontent.getBlogFileName()).list();
			for (String fileImgName : fileNames) {
				BlogMoveCommonUtils.addImgWaterMark(blogcontent.getBlogFileName(), fileImgName, blogMove);
			}
		}

		// 设置其他
		blogcontent.setStatus(blogMove.getMoveBlogStatus());
		blogcontent.setBlogColumnName(blogMove.getMoveColumn());
		// 特殊处理
		blogcontent.setArticleEditor(blogMove.getMoveArticleEditor());
		blogcontent.setShowId(DateUtils.format(new Date(), DateUtils.YYYYMMDDHHMMSSSSS));
		blogcontent.setAllowComment(0);
		blogcontent.setAllowPing(0);
		blogcontent.setAllowDownload(0);
		blogcontent.setShowIntroduction(1);
		blogcontent.setIntroduction("");
		blogcontent.setPrivateArticle(1);

		return blogcontent;
	}

}
