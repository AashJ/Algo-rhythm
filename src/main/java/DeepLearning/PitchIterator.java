package DeepLearning;

import jm.constants.Pitches;
import jm.music.data.Note;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.DataSetPreProcessor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Aashish on 8/21/16.
 */
public class PitchIterator implements DataSetIterator
{
	private int[] validPitches;

	private Map<Note, Integer> pitchToIndexMap;

	Note[] fileNotes;

	private int exampleLength;

	private int miniBatchSize;

	private Random rng;

	private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();

	public PitchIterator(Note[] noteArray, int miniBatchSize, int exampleLength, int[] validPitches, Random rng) throws IOException
	{
		if( miniBatchSize <= 0 ) throw new IllegalArgumentException("Invalid miniBatchSize (must be >0)");

		this.validPitches = validPitches;
		this.exampleLength = exampleLength;
		this.miniBatchSize = miniBatchSize;
		this.rng = rng;

		pitchToIndexMap = new HashMap<>();


		for (int i = 0; i < noteArray.length; i++)
		{
			pitchToIndexMap.put(noteArray[i], i);
		}

		//Note[] newFileNotes = new Note[0];
		//for (int i = 0; i < 10; i++)
		//{
		//	Note[] dupe = noteArray.clone();

		//	newFileNotes = concat(newFileNotes, dupe);

		//	dupe = null;
	//	}

		fileNotes = noteArray;

		//newFileNotes = null;

		if (exampleLength >= fileNotes.length)
		{
			throw new IllegalArgumentException("exampleLength="+exampleLength
					  +" cannot exceed number of valid characters in file ("+fileNotes.length+")");
		}

		int nMinibatchesPerEpoch = (fileNotes.length-1) / exampleLength - 2;

		System.out.println("File Notes: " + fileNotes.length);

		//testing for now to increase the number of notes to ~ 5 million




		for( int i = 0; i< nMinibatchesPerEpoch; i++ )
		{
			exampleStartOffsets.add(i * exampleLength);
		}
		Collections.shuffle(exampleStartOffsets, rng);
	}

	public Note[] concat(Note[] a, Note[] b) {
		int aLen = a.length;
		int bLen = b.length;
		Note[] c= new Note[aLen+bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}

	public static int[] getPitchSet()
	{
		int[] pitchSet = new int[100];
		for (int i = 0; i < pitchSet.length - 1; i++)
		{
			pitchSet[i] = i;
		}

		pitchSet[pitchSet.length - 1] = Pitches.REST;

		return pitchSet;
	}

	public Note convertIndexToNote(int index)
	{
		return fileNotes[index];
	}

	public int convertPitchToIndex(Note n)
	{
		return pitchToIndexMap.get(n);
	}

	public Note getRandomNote()
	{
		return fileNotes[(int) (rng.nextDouble() * fileNotes.length)];
	}

	public boolean hasNext()
	{
		return exampleStartOffsets.size() > 0;
	}

	public DataSet next()
	{
		return next(miniBatchSize);
	}

	public DataSet next(int num)
	{
		if( exampleStartOffsets.size() == 0 ) throw new NoSuchElementException();

		int currMinibatchSize = Math.min(num, exampleStartOffsets.size());

		//Why 'f' order here? See http://deeplearning4j.org/usingrnns.html#data section "Alternative: Implementing a custom DataSetIterator"
		INDArray input = Nd4j.create(new int[]{currMinibatchSize, fileNotes.length, exampleLength}, 'f');
		INDArray labels = Nd4j.create(new int[]{currMinibatchSize, fileNotes.length, exampleLength}, 'f');

		for( int i = 0; i< currMinibatchSize; i++)
		{
			int startIdx = exampleStartOffsets.removeFirst();
			int endIdx = startIdx + exampleLength;
			int currPitchIndex = pitchToIndexMap.get(fileNotes[startIdx]);	//Current input
			int c = 0;
			for( int j=startIdx+1; j<endIdx; j++, c++)
			{
				int nextPitchIndex = pitchToIndexMap.get(fileNotes[j]);		//Next character to predict
				input.putScalar(new int[]{i,currPitchIndex,c}, 1.0);
				labels.putScalar(new int[]{i,nextPitchIndex,c}, 1.0);
				currPitchIndex = nextPitchIndex;
			}
		}

		return new DataSet(input,labels);
	}

	public int totalExamples() {
		return (fileNotes.length-1) / miniBatchSize - 2;
	}

	public int inputColumns() {
		return fileNotes.length;
	}

	public int totalOutcomes() {
		return fileNotes.length;
	}

	public void reset() {
		exampleStartOffsets.clear();
		int nMinibatchesPerEpoch = totalExamples();
		for( int i=0; i<nMinibatchesPerEpoch; i++ ){
			exampleStartOffsets.add(i * miniBatchSize);
		}
		Collections.shuffle(exampleStartOffsets,rng);
	}

	public boolean resetSupported() {
		return true;
	}

	public int batch() {
		return miniBatchSize;
	}

	public int cursor() {
		return totalExamples() - exampleStartOffsets.size();
	}

	public int numExamples() {
		return totalExamples();
	}

	@Override
	public void setPreProcessor(org.nd4j.linalg.dataset.api.DataSetPreProcessor preProcessor)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public void setPreProcessor(DataSetPreProcessor preProcessor) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public DataSetPreProcessor getPreProcessor() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public List<String> getLabels() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}


}

