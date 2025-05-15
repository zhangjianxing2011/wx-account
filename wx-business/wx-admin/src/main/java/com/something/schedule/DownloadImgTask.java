package com.something.schedule;

import com.something.constants.SpiderStatusEnum;
import com.something.utils.OkHttpUtils;
import com.something.dao.domain.SignPictureEntity;
import com.something.dao.service.ISignPictureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
@RequiredArgsConstructor
@Component
public class DownloadImgTask {

    private final ISignPictureService signPictureService;

    @Value("${custom.pic-path}")
    private String picPath;

    ReentrantLock lock = new ReentrantLock();
    @Scheduled(cron = "0 */30 6,7 * * ?")
    public void pictureDownload() {
        try {
            lock.lock();
            List<SignPictureEntity> list = signPictureService.lambdaQuery().eq(SignPictureEntity::getSpiderStatus, SpiderStatusEnum.TODO.getCode()).list();
            for (SignPictureEntity signPicture : list) {
                String url = signPicture.getSignPictureOrigin();
                OkHttpUtils.downloadImageAsync(url, picPath + "/" + signPicture.getSignPicture(), new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        SignPictureEntity failureEntity = new SignPictureEntity();
                        failureEntity.setId(signPicture.getId());
                        failureEntity.setSpiderStatus(SpiderStatusEnum.UNKNOWN.getCode());
                        signPictureService.updateById(failureEntity);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful()) {
                                SignPictureEntity successEntity = new SignPictureEntity();
                                successEntity.setId(signPicture.getId());
                                successEntity.setSpiderStatus(SpiderStatusEnum.DONE.getCode());
                                signPictureService.updateById(successEntity);
                            } else {
                                log.warn("HTTP请求失败: {}", response.code());
                            }
                        } finally {
                            response.close(); // 显式关闭响应
                        }
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws IOException {
        String url = "http://mediatx.ancda.com/0751074017/20240515/171576205308420.jpg";

        OkHttpUtils.downloadImageSync(url, new DownloadImgTask(null).picPath + "/" + "test.jpg");

    }

}
