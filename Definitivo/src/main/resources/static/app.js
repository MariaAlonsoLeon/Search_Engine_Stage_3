document.addEventListener("DOMContentLoaded", () => {
    const root = document.getElementById("root");

    root.innerHTML = `
    <section id="simple-search">
        <h2>Simple Search</h2>
        <input id="simpleSearchInput" type="text" placeholder="Enter a word">
        <button onclick="performSimpleSearch()">Search</button>
        <div id="simpleSearchResults" class="results"></div>
    </section>

    <section id="fuzzy-search">
        <h2>Fuzzy Search</h2>
        <input id="fuzzySearchInput" type="text" placeholder="Enter a word">
        <input id="fuzzySearchTolerance" type="number" placeholder="Tolerance" value="2" min="1">
        <button onclick="performFuzzySearch()">Search</button>
        <div id="fuzzySearchResults" class="results"></div>
    </section>

    <section id="stats">
        <h2>Statistics</h2>
        <select id="statsType">
            <option value="doc_frequency">Document Frequency</option>
            <option value="top_authors">Top Authors</option>
            <option value="language_distribution">Language Distribution</option>
            <option value="release_date_range">Release Date Range</option>
            <option value="word_frequency">Word Frequency</option>
        </select>
        <button onclick="getStats()">Get Stats</button>
        <div id="statsResults" class="results"></div>
    </section>
    `;

    window.performSimpleSearch = async () => {
        const input = document.getElementById("simpleSearchInput").value;
        const resultsContainer = document.getElementById("simpleSearchResults");
        resultsContainer.innerHTML = "Loading...";
        try {
            const response = await fetch(`/search/simple?word=${encodeURIComponent(input)}`);
            const data = await response.json();
            resultsContainer.innerHTML = `
                <h3>Results for "${input}":</h3>
                <pre>${JSON.stringify(data, null, 2)}</pre>
            `;
        } catch (error) {
            resultsContainer.innerHTML = "Error fetching results. Please check the console for details.";
            console.error(error);
        }
    };

    window.performFuzzySearch = async () => {
        const input = document.getElementById("fuzzySearchInput").value;
        const tolerance = document.getElementById("fuzzySearchTolerance").value;
        const resultsContainer = document.getElementById("fuzzySearchResults");
        resultsContainer.innerHTML = "Loading...";
        try {
            const response = await fetch(`/search/fuzzy?word=${encodeURIComponent(input)}&tolerance=${tolerance}`);
            const data = await response.json();
            resultsContainer.innerHTML = `
                <h3>Results for "${input}" (Tolerance: ${tolerance}):</h3>
                <pre>${JSON.stringify(data, null, 2)}</pre>
            `;
        } catch (error) {
            resultsContainer.innerHTML = "Error fetching results. Please check the console for details.";
            console.error(error);
        }
    };

    window.getStats = async () => {
        const type = document.getElementById("statsType").value;
        const resultsContainer = document.getElementById("statsResults");
        resultsContainer.innerHTML = "Loading...";
        try {
            const response = await fetch(`/stats/${type}`);
            const data = await response.json();
            resultsContainer.innerHTML = `
                <h3>Stats Type: "${type}"</h3>
                <pre>${JSON.stringify(data, null, 2)}</pre>
            `;
        } catch (error) {
            resultsContainer.innerHTML = "Error fetching stats. Please check the console for details.";
            console.error(error);
        }
    };
});
