package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.controller.result.WinningRecordResult;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 抽奖相关接口
 * <p>提供执行抽奖、查询中奖记录列表能力。</p>
 */
@RestController
public class DrawPrizeController {

    @Autowired
    private DrawPrizeService drawPrizeService;

    private static final Logger logger = LoggerFactory.getLogger(DrawPrizeController.class);

    /**
     * 执行抽奖
     */
    @RequestMapping("/draw-prize")
    public CommonResult<Boolean> drawPrize(@RequestBody @Validated DrawPrizeParam param) {
        logger.info("drawPrize param: {}", param);
        //service
        drawPrizeService.drawPrize(param);
        return CommonResult.success(true);
    }

    /**
     * 分页查询中奖记录
     */
    @RequestMapping("/winning-records/show")
    public CommonResult<List<WinningRecordResult>> showWinningRecords(
            @Validated @RequestBody ShowWinningRecordsParam param) {
        logger.info("showWinningRecords ShowWinningRecordsParam:{}",
                JacksonUtil.writeValueAsString(param));
        List<WinningRecordDTO> winningRecordDTOList = drawPrizeService.getRecords(param);
        return CommonResult.success(
                convertToWinningRecordResultList(winningRecordDTOList));
    }

    private List<WinningRecordResult> convertToWinningRecordResultList(
            List<WinningRecordDTO> winningRecordDTOList) {
        if (CollectionUtils.isEmpty(winningRecordDTOList)) {
            return Arrays.asList();
        }
        return winningRecordDTOList.stream()
                .map(winningRecordDTO -> {
                    WinningRecordResult result = new WinningRecordResult();
                    result.setWinnerId(winningRecordDTO.getWinnerId());
                    result.setWinnerName(winningRecordDTO.getWinnerName());
                    result.setPrizeName(winningRecordDTO.getPrizeName());
                    result.setPrizeTier(winningRecordDTO.getPrizeTier().getMessage());
                    result.setWinningTime(winningRecordDTO.getWinningTime());
                    return result;
                }).collect(Collectors.toList());
    }

}
