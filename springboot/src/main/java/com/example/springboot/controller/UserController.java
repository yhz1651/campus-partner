package com.example.springboot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;
import java.util.Map.Entry;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.annotation.AutoLog;
import com.example.springboot.entity.*;
import com.example.springboot.service.*;
import com.example.springboot.utils.IpUtils;
import com.example.springboot.utils.SessionUtils;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IRelationService relationService;

    @Resource
    private IPraiseService praiseService;

    @Resource
    private ICommentService commentService;

    @Resource
    private ICollectService collectService;

    @Resource
    private IDynamicService dynamicService;


    // 新增或者更新
    @AutoLog("新增用户")
    @PostMapping
    @SaCheckPermission("user.add")
    public Result save(@RequestBody User user) {
        userService.saveUser(user);
        return Result.success();
    }

    @AutoLog("编辑用户")
    @PutMapping
    @SaCheckPermission("user.edit")
    public Result update(@RequestBody User user) {
        userService.updateById(user);
        return Result.success(user);
    }

    @AutoLog("删除用户")
    @DeleteMapping("/{id}")
    @SaCheckPermission("user.delete")
    public Result delete(@PathVariable Integer id) {
        userService.removeById(id);
        return Result.success();
    }

    @AutoLog("批量删除用户")
    @PostMapping("/del/batch")
    @SaCheckPermission("user.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        userService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaIgnore
    public Result findAll() {
        return Result.success(userService.list());
    }

    @GetMapping("/hot")
    @SaIgnore
    public Result hot() {
        List<User> userList = userService.list(new QueryWrapper<User>().orderByDesc("score"));
        return Result.success(userList.stream().limit(10));
    }

    @GetMapping("/{id}")
    @SaIgnore
    public Result findOne(@PathVariable Integer id) {
        User user = userService.getById(id);
        if (user.getBirthday() != null) {
            user.setAge(DateUtil.ageOfNow(user.getBirthday()));
        }

        List<Relation> relationList = relationService.list();
        List<Praise> praiseList = praiseService.list();
        User currentUser = SessionUtils.getUser(); // 当前的用户
        // 查询是否关注 被关注
        user.setHasFollow(relationList.stream().anyMatch(relation -> relation.getFollowId().equals(currentUser.getId()) && relation.getUserId().equals(user.getId())));
        user.setHasBeFollow(relationList.stream().anyMatch(relation -> relation.getUserId().equals(currentUser.getId()) && relation.getFollowId().equals(user.getId())));

        // 查出多少获赞、关注、粉丝
        int praiseCount = (int) praiseList.stream().filter(praise -> praise.getBepraiseId().equals(user.getId())).count(); // 点赞数
        user.setBePraiseCount(praiseCount); // 获赞
        int followCount = (int) relationList.stream().filter(relation -> relation.getFollowId().equals(user.getId())).count(); // 点赞数
        user.setFollowCount(followCount); // 关注
        int fansCount = (int) relationList.stream().filter(relation -> relation.getUserId().equals(user.getId())).count(); // 点赞数
        user.setBeFollowCount(fansCount); // 粉丝

        return Result.success(user);
    }

    @GetMapping("/page")
    @SaIgnore
    public Result findPage(@RequestParam(defaultValue = "") String username,
                           @RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String address,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().orderByDesc("id");
        queryWrapper.like(StrUtil.isNotBlank(username), "username", username);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(address), "address", address);
        return Result.success(userService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/pageFront")
    @SaIgnore
    public Result findPageFront(@RequestParam(defaultValue = "") String name,
                                @RequestParam Integer pageNum,
                                @RequestParam Integer pageSize) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().orderByDesc("score");
        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like("name", name).or(q -> q.like("username", name)).or(q -> q.like("profile", name));
        } else {
            return Result.success();
        }
        Page<User> page = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<User> records = page.getRecords();
        List<Relation> relationList = relationService.list();
        User user = SessionUtils.getUser(); // 当前的用户
        for (User record : records) {
            // 查询是否关注 被关注
            record.setHasFollow(relationList.stream().anyMatch(relation -> relation.getFollowId().equals(user.getId()) && relation.getUserId().equals(record.getId())));
            record.setHasBeFollow(relationList.stream().anyMatch(relation -> relation.getUserId().equals(user.getId()) && relation.getFollowId().equals(record.getId())));
        }
        return Result.success(page);
    }

    // 好友推荐-协同过滤算法

    /**
     * 好友推荐
     * 基于用户的协同过滤算法
     * User-based Collaborative Filtering
     */
    @GetMapping("/recommend")
    @SaIgnore
    public Result recommend() {
        User currentUser = SessionUtils.getUser();  // 获取当前登录的用户信息
        int recommendUserId = currentUser.getId();
        List<User> userList = userService.list(); // 用户信息，不包含管理员
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
            List<Praise> praiseList = praiseService.list(new QueryWrapper<Praise>().eq("user_id", user.getId()).eq("type", "dynamic"));
            for (Praise praise : praiseList) {
                List<Dynamic> dynamicList = dynamicService.list(new QueryWrapper<Dynamic>().eq("id", praise.getFid()));
                for (Dynamic dynamic : dynamicList) {
                    List<String> topics = dynamic.getTopics();
                    for (String topic : topics) {
                        userItems.get(user.getId()).add(topic); // 用户-话题
                    }
                }
            }
            // 收藏
            List<Collect> collectList = collectService.list(new QueryWrapper<Collect>().eq("user_id", user.getId()));
            for (Collect collect : collectList) {
                List<Dynamic> dynamicList = dynamicService.list(new QueryWrapper<Dynamic>().eq("id", collect.getDynamicId()));
                for (Dynamic dynamic : dynamicList) {
                    List<String> topics = dynamic.getTopics();
                    for (String topic : topics) {
                        userItems.get(user.getId()).add(topic);
                    }
                }
            }
            // 评论
            List<Comment> commentList = commentService.list(new QueryWrapper<Comment>().eq("user_id", user.getId()));
            for (Comment comment : commentList) {
                List<Dynamic> dynamicList = dynamicService.list(new QueryWrapper<Dynamic>().eq("id", comment.getDynamicId()));
                for (Dynamic dynamic : dynamicList) {
                    List<String> topics = dynamic.getTopics();
                    for (String topic : topics) {
                        userItems.get(user.getId()).add(topic);
                    }
                }
            }
            // 发表
            List<Dynamic> dynamicList = dynamicService.list(new QueryWrapper<Dynamic>().eq("user_id", user.getId()));
            for (Dynamic dynamic : dynamicList) {
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
        //计算相似度矩阵【稀疏】
        Set<Entry<String, Set<Integer>>> entrySet = itemUserCollection.entrySet();
        for (Entry<String, Set<Integer>> stringSetEntry : entrySet) {
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

        //计算用户之间的相似度【余弦相似性】
        for (int i = 0; i < N; i++) {
            if(!userItems.containsKey(i)) continue;
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
        List<User> results = new ArrayList<>();
        for (int j = 0; j < N; j++) {
            if (j == recommendUserId) continue;
            if (userItems.containsKey(j))
                idSimilarity.put(j, cosineSparseMatrix[recommendUserId][j]);
        }
        List<Entry<Integer, Double>> list = new ArrayList<>(idSimilarity.entrySet());
        list.sort((d1, d2) -> d2.getValue().compareTo(d1.getValue()));
        for (Entry<Integer, Double> entry : list) {
            User user = userService.getById(entry.getKey());
            if ("USER".equals(user.getRole())) {
                users.add(user);
            }
        }
        List<Relation> relationList = relationService.list();
        // 查询是否关注 被关注
        for (User user : users) {
            user.setHasFollow(relationList.stream().anyMatch(relation -> relation.getFollowId().equals(currentUser.getId()) && relation.getUserId().equals(user.getId())));
            user.setHasBeFollow(relationList.stream().anyMatch(relation -> relation.getUserId().equals(currentUser.getId()) && relation.getFollowId().equals(user.getId())));
            if (!user.getHasFollow()) results.add(user);
        }
        return Result.success(results.stream().limit(8));

        // 固定返回小于等于5的个数
//        Set<User> userSet = new HashSet<>();  // set数据会自动去重
//        if (results.size() <= 5) {
//            return Result.success(results);
//        } else {
//            while (userSet.size() < 5) {
//                int num = RandomUtil.randomInt(0, results.size());  // 获取一个随机的序号
//                userSet.add(results.get(num));
//            }
//            return Result.success(userSet);
//        }
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("user.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<User> list = userService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("User信息表", "UTF-8");
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
    @SaCheckPermission("user.import")
    public Result imp(MultipartFile file) throws Exception {
        // 文件上传
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<User> list = reader.readAll(User.class);

//        userService.saveBatch(list);
        for (User user : list) {
            userService.saveUser(user);
        }
        return Result.success();
    }

    //自定义Entry对象的比较器。每个Entry对象可通过getKey()、getValue()获得Key或Value用于比较。换言之：我们也可以通过Entry对象实现按Key排序。
    static class MyComparator implements Comparator<Map.Entry> {
        public int compare(Map.Entry o1, Map.Entry o2) {
            return ((Double) o1.getValue()).compareTo((Double) o2.getValue());
        }
    }

}
