import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVReader;

public class Sensor {

	private long[] date;
	private double[] bearing;

	public Sensor(final String path) throws IOException {

		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("dd/MM/yyyy HH:mm:ss");

		// Filename containing the data
		String csvFilename = "data/ArcTan_Data.csv";

		// schema
		// 100112 120000,12000.00 ,24000.00 ,0.00 ,-153.43 

		// Reading the csv file but ignoring the first column since it contains
		// headings the result is stored in a list
		CSVReader csvReader = null;

		try {
			csvReader = new CSVReader(new FileReader(csvFilename), ',', '\'', 3);
			final List<String[]> content = csvReader.readAll();
			// variable to hold each row of the List while iterating through it
			String[] row = null;

			// counter used to populate the variables with data from the csv
			// file
			int counter = 0;

			// initializing the variables to hold data in the csv file
			bearing = new double[content.size()];
			date = new long[content.size()];

			for (Object object : content) {
				row = (String[]) object;
				/* parsing data from the list to the variables */
				String thisDate = row[0].toString();
				date[counter] = formatter.parseDateTime(thisDate).getMillis();
				bearing[counter] = (Double.parseDouble(row[4].toString()));
				counter++;
			}
		} finally {
			if(csvReader != null)
				csvReader.close();
		}
	}

	public List<Double> extractBearings(Long start, Long end) {
		List<Double> bearings = new ArrayList<Double>();
		// ok, loop through our data
		for (int i = 0; i < date.length; i++) {
			long thisT = date[i];
			if((thisT >= start) && (thisT <= end))
			{
				bearings.add(bearing[i]);
			}
			
		}
		return bearings;
	}

	public List<Long> extractTimes(Long start, Long end) {
		List<Long> times = new ArrayList<Long>();
		// ok, loop through our data
		for (int i = 0; i < date.length; i++) {
			long thisT = date[i];
			if((thisT >= start) && (thisT <= end))
			{
				times.add(date[i]);
			}
			
		}
		return times;
	}


}
