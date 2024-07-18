package darkchoco.bankstatement.batch;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkLoggingListener implements ChunkListener {

    private static final Logger logger = LoggerFactory.getLogger(ChunkLoggingListener.class);

    @Override
    public void afterChunk(ChunkContext context) {
        long rCount = context.getStepContext().getStepExecution().getReadCount();
        long wCount = context.getStepContext().getStepExecution().getWriteCount();

        logger.info("Processed chunk - Read count: {} / Write count: {}",
                String.format("%,6d", rCount),
                String.format("%,6d", wCount));
    }
}
