package com.example.springboot.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.springboot.common.annotation.AutoLog;
import cn.hutool.core.date.DateUtil;
import com.example.springboot.entity.*;
import com.example.springboot.service.*;
import com.example.springboot.service.impl.UserServiceImpl;
import com.example.springboot.utils.SessionUtils;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.sf.jsqlparser.statement.select.Top;
import org.apache.poi.xssf.model.Comments;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dynamic")
public class DynamicController {

    @Resource
    private IDynamicService dynamicService;

    @Resource
    IUserService userService;

    @Resource
    IPraiseService praiseService;

    @Resource
    ICollectService collectService;

    @Resource
    ITopicService topicService;

    @Resource
    ICommentService commentService;

    @Resource
    IRelationService relationService;

    @AutoLog("新增动态")
    @PostMapping
    @SaIgnore
    public Result save(@RequestBody Dynamic dynamic) {
        User user = SessionUtils.getUser();  // 获取当前登录的用户信息
        dynamic.setUserId(user.getId());
        dynamic.setTime(DateUtil.now());
        dynamicService.save(dynamic);
        // 加积分
        userService.updateScore(5, user.getId());

        // 增加话题热度
        JSONArray array = JSONUtil.parseArray(dynamic.getTopics());
        List<String> topics = JSONUtil.toList(array, String.class);
        for (String topic : topics) {
            Topic topic1 = topicService.getOne(new UpdateWrapper<Topic>().eq("name", topic));
            topicService.updateHot(topic1.getId(), 5); // 热度加5
        }

        return Result.success();
    }

    @AutoLog("编辑动态")
    @PutMapping
    @SaIgnore
    public Result update(@RequestBody Dynamic dynamic) {
        dynamicService.updateById(dynamic);
        // 增加话题热度
        JSONArray array = JSONUtil.parseArray(dynamic.getTopics());
        List<String> topics = JSONUtil.toList(array, String.class);
        for (String topic : topics) {
            Topic topic1 = topicService.getOne(new UpdateWrapper<Topic>().eq("name", topic));
            topicService.updateHot(topic1.getId(), 5); // 热度加5
        }
        return Result.success();
    }

    @AutoLog("删除动态")
    @DeleteMapping("/{id}")
    @SaIgnore
    public Result delete(@PathVariable Integer id) {
        dynamicService.removeById(id);
        return Result.success();
    }

    @AutoLog("批量删除动态")
    @PostMapping("/del/batch")
    @SaCheckPermission("dynamic.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        dynamicService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaCheckPermission("dynamic.list")
    public Result findAll() {
        return Result.success(dynamicService.list());
    }


    // 详情接口
    @GetMapping("/{id}")
    @SaIgnore
    public Result findOne(@PathVariable Integer id) {
        dynamicService.updateView(id);
        Dynamic dynamic = dynamicService.getById(id);
        Optional.of(userService.getById(dynamic.getUserId())).ifPresent(dynamic::setUser);
        // 查到了点赞的数据
        List<Praise> praiseList = praiseService.list();
        List<Collect> collectList = collectService.list();
        List<Comment> commentList = commentService.list();
        List<Relation> relationList = relationService.list();
        User user = SessionUtils.getUser();
        if (user != null) {
            // 筛选了当前用户是否点赞了动态
            dynamic.setHasPraise(praiseList.stream().anyMatch(praise -> praise.getUserId().equals(user.getId()) && praise.getFid().equals(dynamic.getId())));
            dynamic.setHasCollect(collectList.stream().anyMatch(collect -> collect.getUserId().equals(user.getId()) && collect.getDynamicId().equals(dynamic.getId())));
            dynamic.getUser().setHasFollow(relationList.stream().anyMatch(relation -> relation.getFollowId().equals(user.getId()) && relation.getUserId().equals(dynamic.getUserId())));
            dynamic.getUser().setHasBeFollow(relationList.stream().anyMatch(relation -> relation.getUserId().equals(user.getId()) && relation.getFollowId().equals(dynamic.getUserId())));
        }
        // 获取点赞、收藏、评论的数量
        dynamic.setPraiseCount((int) praiseList.stream().filter(praise -> praise.getFid().equals(dynamic.getId())).count());
        dynamic.setCollectCount((int) collectList.stream().filter(collect -> collect.getDynamicId().equals(dynamic.getId())).count());
        dynamic.setCommentCount((int) commentList.stream().filter(comment -> comment.getDynamicId().equals(dynamic.getId())).count());

        return Result.success(dynamic);
    }

    // 热门动态
    @GetMapping("/hot")
    @SaIgnore
    public Result hot() {
        List<Dynamic> list = dynamicService.list();
        List<Praise> praiseList = praiseService.list();
        List<Collect> collectList = collectService.list();
        List<Comment> commentList = commentService.list();
        for (Dynamic dynamic : list) {
            int praiseCount = (int) praiseList.stream().filter(p -> p.getFid().equals(dynamic.getId())).count();  // 点赞的个数
            int collectCount = (int) collectList.stream().filter(p -> p.getDynamicId().equals(dynamic.getId())).count();  // 点赞的个数
            int commentCount = (int) commentList.stream().filter(p -> p.getDynamicId().equals(dynamic.getId())).count();  // 点赞的个数
            //  计算热度 点赞的权重是2  收藏的权重也是2  浏览的权重是1 评论是3
            dynamic.setHot(praiseCount * 2 + collectCount * 2 + commentCount * 3 + dynamic.getView());
        }
        List<Relation> relationList = relationService.list();
        List<User> userList = userService.list();
        User user = SessionUtils.getUser();
        for (Dynamic dynamic : list) {
            // 查出用户信息
            userList.stream().filter(u -> u.getId().equals(dynamic.getUserId())).findFirst().ifPresent(dynamic::setUser);

            // 查出多少点赞、收藏、评论
            int priseCount = (int) praiseList.stream().filter(praise -> praise.getFid().equals(dynamic.getId())).count(); // 点赞数
            dynamic.setPraiseCount(priseCount);
            int collectCount = (int) collectList.stream().filter(collect -> collect.getDynamicId().equals(dynamic.getId())).count(); // 点赞数
            dynamic.setCollectCount(collectCount);
            int commentsCount = (int) commentList.stream().filter(comments -> comments.getDynamicId().equals(dynamic.getId())).count(); // 点赞数
            dynamic.setCommentCount(commentsCount);

            dynamic.getUser().setHasFollow(relationList.stream().anyMatch(relation -> relation.getFollowId().equals(user.getId()) && relation.getUserId().equals(dynamic.getUserId())));
            dynamic.getUser().setHasBeFollow(relationList.stream().anyMatch(relation -> relation.getUserId().equals(user.getId()) && relation.getFollowId().equals(dynamic.getUserId())));
        }
        // 取热度最高的10条动态
        return Result.success(list.stream().sorted((d1, d2) -> d2.getHot().compareTo(d1.getHot())).limit(10));
    }

    /**
     * 动态推荐 协同过滤算法
     */
    @GetMapping("/recommend")
    @SaIgnore
    public Result recommend() {
        List<User> userList = userService.list();
        List<Praise> praiseList = praiseService.list();
        List<Collect> collectList = collectService.list();
        List<Comment> commentList = commentService.list();
        List<Relation> relationList = relationService.list();
        List<Dynamic> dynamicList = dynamicService.list();
        User currentUser = SessionUtils.getUser();  // 获取当前登录的用户信息
        int recommendUserId = currentUser.getId();
        int N = 0;
        for (User user : userList) {
            N = Math.max(N, user.getId());
        }
        N++;
        int[][] sparseMatrix = new int[N][N]; // 建立用户稀疏矩阵，用于用户相似度计算【相似度矩阵】
        double[][] cosineSparseMatrix = new double[N][N]; // 余弦相似度计算
        Map<Integer, Set<String>> userItems = new HashMap<>(); // 用户-物品的映射
        Map<String, Set<Integer>> itemUserCollection = new HashMap<>(); // 建立物品到用户的倒排表 eg: a A B
        Set<String> items = new HashSet<>(); // 辅助存储物品集合
        for (User user : userList) {
            userItems.put(user.getId(), new HashSet<String>());
            // 点赞
            List<Praise> praises = praiseService.list(new QueryWrapper<Praise>().eq("user_id", user.getId()).eq("type", "dynamic"));
            for (Praise praise : praises) {
                List<Dynamic> dynamics = dynamicService.list(new QueryWrapper<Dynamic>().eq("id", praise.getFid()));
                for (Dynamic dynamic : dynamics) {
                    List<String> topics = dynamic.getTopics();
                    for (String topic : topics) {
                        userItems.get(user.getId()).add(topic); // 用户-话题
                    }
                }
            }
            // 收藏
            List<Collect> collects = collectService.list(new QueryWrapper<Collect>().eq("user_id", user.getId()));
            for (Collect collect : collects) {
                List<Dynamic> dynamics = dynamicService.list(new QueryWrapper<Dynamic>().eq("id", collect.getDynamicId()));
                for (Dynamic dynamic : dynamics) {
                    List<String> topics = dynamic.getTopics();
                    for (String topic : topics) {
                        userItems.get(user.getId()).add(topic);
                    }
                }
            }
            // 评论
            List<Comment> comments = commentService.list(new QueryWrapper<Comment>().eq("user_id", user.getId()));
            for (Comment comment : comments) {
                List<Dynamic> dynamics = dynamicService.list(new QueryWrapper<Dynamic>().eq("id", comment.getDynamicId()));
                for (Dynamic dynamic : dynamics) {
                    List<String> topics = dynamic.getTopics();
                    for (String topic : topics) {
                        userItems.get(user.getId()).add(topic);
                    }
                }
            }
            // 发表
            List<Dynamic> dynamics = dynamicService.list(new QueryWrapper<Dynamic>().eq("user_id", user.getId()));
            for (Dynamic dynamic : dynamics) {
                List<String> topics = dynamic.getTopics();
                for (String topic : topics) {
                    userItems.get(user.getId()).add(topic);
                }
            }

            if (userItems.get(user.getId()).size() == 0) {
                userItems.remove(user.getId());
            } else {
                // 建立物品--用户倒排表
                for (String topic : userItems.get(user.getId())) {
                    if (items.contains(topic)) { // 如果已经包含对应的物品--用户映射，直接添加对应的用户
                        itemUserCollection.get(topic).add(user.getId());
                    } else { // 否则创建对应物品--用户集合映射
                        items.add(topic);
                        itemUserCollection.put(topic, new HashSet<Integer>());//创建物品--用户倒排关系
                        itemUserCollection.get(topic).add(user.getId());
                    }
                }
            }
        }
        // 计算相似度矩阵【稀疏】
        Set<Map.Entry<String, Set<Integer>>> entrySet = itemUserCollection.entrySet();
        for (Map.Entry<String, Set<Integer>> stringSetEntry : entrySet) {
            Set<Integer> commonUsers = stringSetEntry.getValue();
            for (int user_u : commonUsers) {
                for (int user_v : commonUsers) {
                    if (user_u == user_v) {
                        continue;
                    }
                    sparseMatrix[user_u][user_v] += 1; // 计算用户u与用户v都有正反馈的物品总数
                }
            }
        }
        // 计算用户之间的相似度【余弦相似性】
        for (int i = 0; i < N; i++) {
            if (!userItems.containsKey(i)) continue;
            for (int j = 0; j < N; j++) {
                if (i == j) continue;
                if (userItems.containsKey(j)) {
                    int size1 = userItems.get(i).size();
                    int size2 = userItems.get(j).size();
                    cosineSparseMatrix[i][j] = (double) sparseMatrix[i][j] / Math.sqrt(size1 * size2);
                }
            }
        }

        Map<Integer, Double> idSimilarity = new HashMap<>();
        List<User> users = new ArrayList<>();
        for (int j = 0; j < N; j++) {
            if (j == recommendUserId) continue;
            if (userItems.containsKey(j))
                idSimilarity.put(j, cosineSparseMatrix[recommendUserId][j]);
        }
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(idSimilarity.entrySet());
        list.sort((d1, d2) -> d2.getValue().compareTo(d1.getValue()));
        for (Map.Entry<Integer, Double> entry : list) {
            User user = userService.getById(entry.getKey());
            if ("USER".equals(user.getRole())) users.add(user); // 不推荐管理员用户
        }
        Set<String> topicSet = new HashSet<>();

        // 选取相似度最高的5个邻居
        for (User user : users.subList(0, 5)) {
            topicSet.addAll(userItems.get(user.getId()));
        }

        // 计算指定用户recommendUser的物品推荐度
        Map<String, Double> topicRecommend = new HashMap<>();
        Map<Integer, Double> dynamicRecommend = new HashMap<>();

        for (String topic : topicSet) {
            Set<Integer> userIds = new HashSet<>();
            if (itemUserCollection.containsKey(topic)) {
                userIds = itemUserCollection.get(topic); // 得到购买当前物品的所有用户集合
            }
            double itemRecommendDegree = 0.0;
            for (Integer userId : userIds) {
                if (userItems.containsKey(userId) && userItems.containsKey(recommendUserId)) {
                    itemRecommendDegree += sparseMatrix[recommendUserId][userId] / Math.sqrt(userItems.get(recommendUserId).size() * userItems.get(userId).size()); // 推荐度计算
                }
            }
            topicRecommend.put(topic, itemRecommendDegree);
        }
        for (Dynamic dynamic : dynamicList) {
            double dynamicRecommendDegree = 0.0;
            List<String> topics = dynamic.getTopics();
            for (String topic : topics) {
                if (topicSet.contains(topic)) dynamicRecommendDegree += topicRecommend.get(topic);
            }
            dynamicRecommend.put(dynamic.getId(), dynamicRecommendDegree);
        }
        List<Map.Entry<Integer, Double>> list2 = new ArrayList<>(dynamicRecommend.entrySet());
        list2.sort((d1, d2) -> d2.getValue().compareTo(d1.getValue())); // 降序排序
        List<Dynamic> dynamics = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : list2) {
//            if (dynamicRecommend.get(entry.getKey()) < 5) continue;
            Dynamic dynamic = dynamicService.getById(entry.getKey());
            // 查出用户信息
            userList.stream().filter(u -> u.getId().equals(dynamic.getUserId())).findFirst().ifPresent(dynamic::setUser);
            // 查出多少点赞、收藏、评论
            int priseCount = (int) praiseList.stream().filter(praise -> praise.getFid().equals(dynamic.getId())).count(); // 点赞数
            dynamic.setPraiseCount(priseCount);
            int collectCount = (int) collectList.stream().filter(collect -> collect.getDynamicId().equals(dynamic.getId())).count(); // 点赞数
            dynamic.setCollectCount(collectCount);
            int commentsCount = (int) commentList.stream().filter(comments -> comments.getDynamicId().equals(dynamic.getId())).count(); // 点赞数
            dynamic.setCommentCount(commentsCount);

            dynamic.getUser().setHasFollow(relationList.stream().anyMatch(relation -> relation.getFollowId().equals(recommendUserId) && relation.getUserId().equals(dynamic.getUserId())));
            dynamic.getUser().setHasBeFollow(relationList.stream().anyMatch(relation -> relation.getUserId().equals(recommendUserId) && relation.getFollowId().equals(dynamic.getUserId())));
            dynamic.setHasPraise(praiseList.stream().anyMatch(praise -> praise.getUserId().equals(recommendUserId) && praise.getFid().equals(dynamic.getId())));
            dynamic.setHasCollect(collectList.stream().anyMatch(collect -> collect.getUserId().equals(recommendUserId) && collect.getDynamicId().equals(dynamic.getId())));
            boolean hasComment = commentList.stream().anyMatch(comment -> comment.getUserId().equals(recommendUserId) && comment.getDynamicId().equals(dynamic.getId()));
            if (!dynamic.getHasPraise() && !dynamic.getHasCollect() && !hasComment && !Objects.equals(dynamic.getUserId(), currentUser.getId())) { // 推荐没有点赞、收藏、评论过的动态，不推荐自己发表的动态
                dynamics.add(dynamic);
            }
        }
        return Result.success(dynamics.stream().limit(10));
    }

    @GetMapping("/page")
    @SaIgnore
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String type, // 区分用户
                           @RequestParam(defaultValue = "0") Integer typeId, // 区分用户id
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Dynamic> queryWrapper = new QueryWrapper<Dynamic>().orderByDesc("time");
        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like("name", name).or(q -> q.like("topics", name)).or(q -> q.like("content", name)); // 根据标题、内容、话题模糊搜索
        }

        // 权限隔离，普通用户只能看到自己的数据，管理员可以看到所有的数据
        User user = SessionUtils.getUser(); // 当前的用户

        if ("user".equals(type) && typeId == 0) {  // 如果type是user，typeId=0 ,表示筛选用户自己的数据
            queryWrapper.eq("user_id", user.getId());  // select * from  dynamic where user_id = xxx
        } else if ("user".equals(type) && typeId != 0) { // // 如果type是user，typeId!=0 , 表示筛选用户typeId的数据
            queryWrapper.eq("user_id", typeId);  // select * from  dynamic where user_id = xxx
        }

        if ("collect".equals(type)) {  // 如果type是collect，表示筛选用户收藏的数据
            List<Collect> dynamicIds = collectService.list(new QueryWrapper<Collect>().eq("user_id", user.getId()));
            if (dynamicIds.size() == 0) { // 没有收藏时直接返回空结果
                return Result.success();
            }
            int cnt = 1;
            for (Collect dynamicId : dynamicIds) {
                if (cnt == 1) {
                    queryWrapper.eq("id", dynamicId.getDynamicId());
                } else {
                    queryWrapper.or().eq("id", dynamicId.getDynamicId());
                }
                cnt++;
            }
        }

        Page<Dynamic> page = dynamicService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Dynamic> records = page.getRecords();
        List<User> userList = userService.list();
        List<Praise> praiseList = praiseService.list();
        List<Collect> collectList = collectService.list();
        List<Comment> commentList = commentService.list();
        List<Relation> relationList = relationService.list();
        for (Dynamic record : records) {
            // 查出用户信息
            userList.stream().filter(u -> u.getId().equals(record.getUserId())).findFirst().ifPresent(record::setUser);

            // 查出多少点赞、收藏、评论
            int priseCount = (int) praiseList.stream().filter(praise -> praise.getFid().equals(record.getId())).count(); // 点赞数
            record.setPraiseCount(priseCount);
            int collectCount = (int) collectList.stream().filter(collect -> collect.getDynamicId().equals(record.getId())).count(); // 点赞数
            record.setCollectCount(collectCount);
            int commentsCount = (int) commentList.stream().filter(comments -> comments.getDynamicId().equals(record.getId())).count(); // 点赞数
            record.setCommentCount(commentsCount);

            // 查询是否关注 被关注
            record.getUser().setHasFollow(relationList.stream().anyMatch(relation -> relation.getFollowId().equals(user.getId()) && relation.getUserId().equals(record.getUserId())));
            record.getUser().setHasBeFollow(relationList.stream().anyMatch(relation -> relation.getUserId().equals(user.getId()) && relation.getFollowId().equals(record.getUserId())));

        }
        return Result.success(page);
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("dynamic.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Dynamic> list = dynamicService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Dynamic信息表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();

    }

    /**
     * excel 导入
     *
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    @SaCheckPermission("dynamic.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Dynamic> list = reader.readAll(Dynamic.class);

        dynamicService.saveBatch(list);
        return Result.success();
    }

}
