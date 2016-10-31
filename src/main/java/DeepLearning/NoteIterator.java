package DeepLearning;

import jm.music.data.Note;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.factory.Nd4j;

import java.util.*;

/**
 * Created by Aashish on 9/24/16.
 */
public class NoteIterator implements DataSetIterator
{
	private Note[] validNotes;
	//Maps each character to an index ind the input/output
	private Map<Note, Integer> noteToIdxMap;
	//All characters of the input file (after filtering to only those that are valid
	private Note[] fileNotes;
	//Length of each example/minibatch (number of characters)
	private int exampleLength;
	//Size of each minibatch (number of examples)
	private int miniBatchSize;
	private Random rng;
	//Offsets for the start of each example
	private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();

	public NoteIterator(Note[] noteArray, int miniBatchSize, int exampleLength, Random rng)
	{
		this.validNotes = noteArray;

		this.exampleLength = exampleLength;
		this.miniBatchSize = miniBatchSize;
		this.rng = rng;

		Note[] newFileNotes = new Note[0];
		for (int i = 0; i < 200; i++)
		{
			Note[] dupe = noteArray.clone();

			newFileNotes = concat(newFileNotes, dupe);

			dupe = null;
		}

		fileNotes = newFileNotes;

		noteToIdxMap = new HashMap<>();

		for (int i = 0; i < validNotes.length; i++)
		{
			noteToIdxMap.put(validNotes[i], i);
		}

		initializeOffsets();
	}

	public Note[] concat(Note[] a, Note[] b) {
		int aLen = a.length;
		int bLen = b.length;
		Note[] c= new Note[aLen+bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}

	private void initializeOffsets() {
		//This defines the order in which parts of the file are fetched
		int nMinibatchesPerEpoch = (fileNotes.length - 1) / exampleLength - 2;   //-2: for end index, and for partial example
		for (int i = 0; i < nMinibatchesPerEpoch; i++) {
			exampleStartOffsets.add(i * exampleLength);
		}
		Collections.shuffle(exampleStartOffsets, rng);
	}

	public Note convertIndexToNote(int idx)
	{
		return validNotes[idx];
	}

	public int convertNoteToIndex(Note n)
	{
		return noteToIdxMap.get(n);
	}

	public Note getRandomNote()
	{
		return validNotes[(int) (rng.nextDouble() * validNotes.length)];
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
		//Allocate space:
		//Note the order here:
		// dimension 0 = number of examples in minibatch
		// dimension 1 = size of each vector (i.e., number of characters)
		// dimension 2 = length of each time series/example
		//Why 'f' order here? See http://deeplearning4j.org/usingrnns.html#data section "Alternative: Implementing a custom DataSetIterator"
		INDArray input = Nd4j.create(new int[]{currMinibatchSize,validNotes.length,exampleLength}, 'f');
		INDArray labels = Nd4j.create(new int[]{currMinibatchSize,validNotes.length,exampleLength}, 'f');

		for( int i=0; i<currMinibatchSize; i++ )
		{
			int startIdx = exampleStartOffsets.removeFirst();
			int endIdx = startIdx + exampleLength;
			int currNoteIdx = noteToIdxMap.get(fileNotes[startIdx]);	//Current input
			int c=0;
			for( int j=startIdx+1; j<endIdx; j++, c++ )
			{
				int nextNoteIdx = noteToIdxMap.get(fileNotes[j]);		//Next character to predict
				input.putScalar(new int[]{i,currNoteIdx,c}, 1.0);
				labels.putScalar(new int[]{i,nextNoteIdx,c}, 1.0);
				currNoteIdx = nextNoteIdx;
			}
		}

		return new DataSet(input,labels);
	}

	public int totalExamples()
	{
		return (fileNotes.length-1) / miniBatchSize - 2;
	}

	public int inputColumns()
	{
		return validNotes.length;
	}

	public int totalOutcomes()
	{
		return validNotes.length;
	}

	public void reset()
	{
		exampleStartOffsets.clear();
		initializeOffsets();
	}

	public boolean resetSupported() {
		return true;
	}

	public boolean asyncSupported() {
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
