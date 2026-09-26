package com.example.lotterysystem;

import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 抽奖服务集成测试：覆盖抽奖执行、活动状态流转、保存中奖记录、查询中奖记录。
 *
 * @author: yibo
 */
@SpringBootTest
public class DrawPrizeTest {

    @Autowired
    private DrawPrizeService drawPrizeService;

    @Autowired
    private ActivityStatusManager activityStatusManager;


    @Test
    void drawPrize() {
//        DrawPrizeParam param = new DrawPrizeParam();
//        param.setActivityId(1L);
//        param.setPrizeId(1L);
//        param.setPrizeTiers("FIRST_PRIZE");
//        param.setWinningTime(new Date());
//        List<DrawPrizeParam.Winner> winnerList = new ArrayList<>();
//        DrawPrizeParam.Winner winner = new DrawPrizeParam.Winner();
//        winner.setUserId(1L);
//        winner.setUserName("xxx");
//        winnerList.add(winner);
//        param.setWinnerList(winnerList);
//        drawPrizeService.drawPrize(param);

        // 1、正向流程
        // 2、处理过程中发生异常：回滚
        // 3、处理过程中发生异常：消息堆积-》处理异常-》消息重发
        DrawPrizeParam param = new DrawPrizeParam();
        param.setActivityId(25L);
        param.setPrizeId(19L);
        param.setWinningTime(new Date());
        List<DrawPrizeParam.Winner> winnerList = new ArrayList<>();
        DrawPrizeParam.Winner winner = new DrawPrizeParam.Winner();
        winner.setUserId(45L);
        winner.setUserName("杨康");
        winnerList.add(winner);
        param.setWinnerList(winnerList);
        drawPrizeService.drawPrize(param);

    }

    @Test
    void statusConvert() {

        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(24L);
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.COMPLETED);
        convertActivityStatusDTO.setPrizeId(19L);
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.COMPLETED);
        List<Long> userIds = Arrays.asList(45L);
        convertActivityStatusDTO.setUserIds(userIds);
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.COMPLETED);
        activityStatusManager.handlerEvent(convertActivityStatusDTO);

    }

    @Test
    void saveWinningRecords(){
        DrawPrizeParam param = new DrawPrizeParam();
        param.setActivityId(24L);
        param.setPrizeId(19L);
        param.setWinningTime(new Date());
        List<DrawPrizeParam.Winner> winnerList = new ArrayList<>();
        DrawPrizeParam.Winner winner = new DrawPrizeParam.Winner();
        winner.setUserId(45L);
        winner.setUserName("杨康");
        winnerList.add(winner);
        param.setWinnerList(winnerList);
        drawPrizeService.saveWinnerRecords(param);
    }


    @Test
    void showWinningRecords() {
        ShowWinningRecordsParam param = new ShowWinningRecordsParam();
        param.setActivityId(25L);
        param.setPrizeId(18L);
        List<WinningRecordDTO> list = drawPrizeService.getRecords(param);
        for (WinningRecordDTO dto : list) {
            // 中奖者_奖品_等级
            System.out.println(dto.getWinnerName()
                    + "_" + dto.getPrizeName()
                    + "_" +dto.getPrizeTier().getMessage());
        }
    }


}
