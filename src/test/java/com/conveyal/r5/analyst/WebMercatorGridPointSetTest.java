package com.conveyal.r5.analyst;

import gnu.trove.list.TIntList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

import java.util.Arrays;
import java.util.List;

import static com.conveyal.r5.analyst.WebMercatorGridPointSetTest.TestEnvelope.Category.INSIDE;
import static com.conveyal.r5.analyst.WebMercatorGridPointSetTest.TestEnvelope.Category.OUTSIDE;
import static com.conveyal.r5.analyst.WebMercatorGridPointSetTest.TestEnvelope.Category.PARTIAL;
import static com.conveyal.r5.common.GeometryUtils.floatingWgsEnvelopeToFixed;

/**
 * Test the web mercator grid pointset.
 */
public class WebMercatorGridPointSetTest {

    /** Test that latitude/longitude to pixel conversions are correct */
    @Test
    public void testLatLonPixelConversions () {
        // offsets and width/height not needed for calculations
        WebMercatorGridPointSet ps = new WebMercatorGridPointSet(
            WebMercatorGridPointSet.DEFAULT_ZOOM, 0,0, 100, 100, null
        );

        for (double lat : new double [] { -75, -25, 0, 25, 75 }) {
            Assertions.assertEquals(lat, ps.pixelToLat(ps.latToPixel(lat)), 1e-2);
        }

        for (double lon : new double [] { -175, -90, 0, 90, 175}) {
            Assertions.assertEquals(lon, ps.pixelToLon(ps.lonToPixel(lon)), 1e-2);
        }
    }

    /** Test that we can find pixel numbers for envelopes intersecting a gridded PointSet. */
    @Test
    public void testPixelsInEnvelope () {

        // Envelope and grid 1 degree high and 5 degrees wide, in the ocean south of Africa.
        Envelope gridEnvelope = new Envelope(10, 15, -45, -44);
        WebMercatorGridPointSet gridPointSet = new WebMercatorGridPointSet(gridEnvelope);

        List<TestEnvelope> envelopes = Arrays.asList(
            new TestEnvelope("Entirely outside the grid, to the north of it.", OUTSIDE,
                12, 13, -43.5, -43.4),
            new TestEnvelope("Entirely outside the grid, to the south of it.", OUTSIDE,
                13, 14, -46, -45.9),
            new TestEnvelope("Entirely outside the grid, to the east of it.", OUTSIDE,
                15.9, 16, -45, -44),
            new TestEnvelope("Entirely outside the grid, to the west of it.", OUTSIDE,
                9, 9.1, -45, -44),
            new TestEnvelope("Entirely inside the grid, on the west side.", INSIDE,
                11, 12, -44.4, -44.6),
            new TestEnvelope("Entirely inside the grid, on the east side.", INSIDE,
                13, 14, -44.5, -44.7),
            new TestEnvelope("Partially overlapping the grid, on the north edge.", PARTIAL,
                12, 12.5, -44.1, -43.9),
            new TestEnvelope("Partially overlapping the grid, on the south edge.", PARTIAL,
                13, 13.5, -45.1, -44.9),
            new TestEnvelope("Partially overlapping the grid, on the east edge.", PARTIAL,
                14.9, 15.1, -44.4, -44.6),
            new TestEnvelope("Partially overlapping the grid, on the west edge.", PARTIAL,
                9.9, 10.1, -44.2, -44.3)
        );
        // Check that expected quantities of points are found depending on the category.
        // Also check that our envelopes actually had the intended characteristics (according to JTS predicates).
        for (TestEnvelope testEnvelope : envelopes) {
            Envelope envelope = testEnvelope.envelope;
            TIntList points = gridPointSet.getPointsInEnvelope(floatingWgsEnvelopeToFixed(envelope));
            if (testEnvelope.category == OUTSIDE) {
                Assertions.assertFalse(gridEnvelope.intersects(envelope));
                Assertions.assertTrue(points.isEmpty());
            } else if (testEnvelope.category == INSIDE) {
                Assertions.assertTrue(gridEnvelope.contains(envelope));
                Assertions.assertTrue(points.size() > 30_000);
            } else if (testEnvelope.category == PARTIAL) {
                Assertions.assertTrue(gridEnvelope.intersects(envelope));
                Assertions.assertFalse(gridEnvelope.contains(envelope));
                Assertions.assertTrue(points.size() > 1_000);
                Assertions.assertTrue(points.size() < 10_000);
            } else {
                throw new AssertionError("Unknown category: " + testEnvelope.category);
            }
            // Every reported point index should fall within the total size of the grid.
            points.forEach(p -> {
                Assertions.assertTrue(p >= 0);
                Assertions.assertTrue(p < gridPointSet.featureCount());
                return true;
            });
        }
    }

    public static class TestEnvelope {
        final String description;
        final Category category;
        final Envelope envelope;

        public TestEnvelope (
                String description,
                Category category,
                double minLon,
                double maxLon,
                double minLat,
                double maxLat
        ) {
            this.description = description;
            this.category = category;
            this.envelope = new Envelope(minLon, maxLon, minLat, maxLat);
        }
        public enum Category {
            INSIDE, OUTSIDE, PARTIAL
        }
    }

}
