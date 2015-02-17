import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVReader;

public class Track {

	final private org.joda.time.DateTime[] dates;
	final private double[] x;
	final private double[] y;
	final private double[] courses;
	final private double[] speeds;

	public org.joda.time.DateTime[] getDates() {
		return dates;
	}

	public double[] getX() {
		return x;
	}

	public double[] getY() {
		return y;
	}

	public double[] getCourses() {
		return courses;
	}

	public double[] getSpeeds() {
		return speeds;
	}

	public Track(final String path) throws IOException {

		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("dd/MM/yyyy HH:mm:ss");

		// Filename containing the data
		String csvFilename = "data/ArcTan_Data.csv";

		// schema
		// 100112 120000,SENSOR,12000.00 ,24000.00 ,0.00 ,205.00 ,4.12 ,100.00
		// ,inactive

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
			x = new double[content.size()];
			y = new double[content.size()];
			speeds = new double[content.size()];
			courses = new double[content.size()];
			dates = new DateTime[content.size()];

			for (Object object : content) {
				row = (String[]) object;
				/* parsing data from the list to the variables */
				String thisDate = row[0].toString();
				dates[counter] = formatter.parseDateTime(thisDate);
				x[counter] = (Double.parseDouble(row[1].toString()));
				y[counter] = (Double.parseDouble(row[2].toString()));
				courses[counter] = (Double.parseDouble(row[4].toString()));
				speeds[counter] = (Double.parseDouble(row[5].toString()));
				counter++;
			}
		} finally {
			if(csvReader != null)
				csvReader.close();
		}
	}

}
