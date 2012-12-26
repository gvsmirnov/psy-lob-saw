package alignment;

import java.nio.ByteBuffer;

import util.UnsafeAccess;
import static util.UnsafeDirectByteBuffer.*;

import com.google.caliper.Param;
import com.google.caliper.SimpleBenchmark;

public class UnalignedMemoryAccessCostBenchmark extends SimpleBenchmark {
    @Param(value = "1")
    int offset;

    // buffy is a page aligned buffer, and a vampire slayer
    private ByteBuffer buffy = allocateAlignedByteBuffer(PAGE_SIZE, PAGE_SIZE);

    public int timeOffsetLongAccess(final int reps) throws InterruptedException {
	long remaining = 0;
	long startingAddress = getAddress(buffy);
	// skip first line if not straddling 2 cache lines
	if (offset + 8 < CACHE_LINE_SIZE) {
	    startingAddress += CACHE_LINE_SIZE + offset;
	} else {
	    startingAddress += offset;
	}
	final long limit = getAddress(buffy) + PAGE_SIZE;
	for (long i = 0; i < reps; i++) {
	    remaining = writeAndRead(i, startingAddress, limit);
	}
	return (int) remaining;
    }

    private long writeAndRead(final long value, final long startingAddress,
	    final long limit) {
	long address = startingAddress;
	for (int i = 0; i < 100; i++) {
	    for (address = startingAddress; address < limit; address += CACHE_LINE_SIZE) {
		UnsafeAccess.unsafe.putLong(address, value);
	    }
	    for (address = startingAddress; address < limit; address += CACHE_LINE_SIZE) {
		if (UnsafeAccess.unsafe.getLong(address) != value)
		    throw new RuntimeException();
	    }
	}
	return limit - address + CACHE_LINE_SIZE;
    }
}
