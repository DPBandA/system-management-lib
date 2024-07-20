/*
Business Entity Library (BEL) - A foundational library for JSF web applications 
Copyright (C) 2024  D P Bennett & Associates Limited

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

Email: info@dpbennett.com.jm
 */
package jm.com.dpbennett.tm.chart;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

@SuppressWarnings("serial")
public class CustomHighLowItemLabelGenerator extends HighLowItemLabelGenerator {

    private DateFormat dateFormatter;
    private NumberFormat numberFormatter;

    /**
     * Creates a tool tip generator using the supplied date formatter.
     *
     * @param dateFormatter the date formatter (<code>null</code> not
     * permitted).
     * @param numberFormatter the number formatter (<code>null</code> not
     * permitted).
     */
    public CustomHighLowItemLabelGenerator(DateFormat dateFormatter, NumberFormat numberFormatter) {
        if (dateFormatter == null) {
            throw new IllegalArgumentException("Null 'dateFormatter' argument.");
        }
        if (numberFormatter == null) {
            throw new IllegalArgumentException("Null 'numberFormatter' argument.");
        }
        this.dateFormatter = dateFormatter;
        this.numberFormatter = numberFormatter;
    }

    /**
     * Generates a tooltip text item for a particular item within a series.
     *
     * @param dataset the dataset.
     * @param series the series (zero-based index).
     * @param item the item (zero-based index).
     *
     * @return The tooltip text.
     */
    @Override
    public String generateToolTip(XYDataset dataset, int series, int item) {

        String result = null;

        if (dataset instanceof OHLCDataset) {
            OHLCDataset d = (OHLCDataset) dataset;
            Number high = d.getHigh(series, item);
            Number low = d.getLow(series, item);
            Number open = d.getOpen(series, item);
            Number close = d.getClose(series, item);
            Number x = d.getX(series, item);

            result = d.getSeriesKey(series).toString();

            if (x != null) {
                Date date = new Date(x.longValue());
                result = result + "--> Time=" + this.dateFormatter.format(date);
                if (high != null) {
                    result = result + " High=" + this.numberFormatter.format(high.doubleValue());
                }
                if (low != null) {
                    result = result + " Low=" + this.numberFormatter.format(low.doubleValue());
                }
                if (open != null) {
                    result = result + " Open=" + this.numberFormatter.format(open.doubleValue());
                }
                if (close != null) {
                    result = result + " Close=" + this.numberFormatter.format(close.doubleValue());
                }
            }

        }

        return result;

    }

}
