import { useEffect, useState } from 'react';
import API from "./api"; 
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const initialStatus = {
  memory: { total: 0, used: 0, available: 0, percent: 0 },
  prediction: 0,
  action: '',
};

function formatTimestamp(timestamp) {
  return new Date(timestamp).toLocaleString();
}

function App() {
  const [status, setStatus] = useState(initialStatus);
  const [recentLogs, setRecentLogs] = useState([]);
  const [alertLogs, setAlertLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAll = async () => {
    try {
      const [statusRes, recentRes, alertRes] = await Promise.all([
        fetch(`${API}/status`),
        fetch(`${API}/logs/recent`),
        fetch(`${API}/logs/alerts`),
      ]);

      if (!statusRes.ok || !recentRes.ok || !alertRes.ok) {
        throw new Error('Failed to fetch monitoring data.');
      }

      const statusData = await statusRes.json();
      const recentData = await recentRes.json();
      const alertData = await alertRes.json();

      setStatus(statusData);
      setRecentLogs(recentData);
      setAlertLogs(alertData);
      setLoading(false);
      setError(null);
    } catch (err) {
      setError(err.message || 'Unable to load data.');
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAll();
    const interval = setInterval(fetchAll, 5000);
    return () => clearInterval(interval);
  }, []);

  const chartLabels = [...recentLogs].reverse().map((record) => formatTimestamp(record.createdAt));
  const chartDataset = [...recentLogs].reverse().map((record) => record.percent);

  const chartData = {
    labels: chartLabels,
    datasets: [
      {
        label: 'Memory Usage (%)',
        data: chartDataset,
        borderColor: '#6366f1',
        backgroundColor: 'rgba(99, 102, 241, 0.18)',
        tension: 0.3,
        fill: true,
        pointRadius: 4,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: { position: 'top' },
      title: { display: true, text: 'This graph shows how system memory usage changes over time.' },
    },
    scales: {
      y: { beginAtZero: true, max: 100 },
    },
  };

  const { memory, prediction, action } = status;

  return (
    <div className="page-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">System Health Dashboard</p>
          <h1>Memory Monitoring System</h1>
        </div>
        <div className="status-pill">Live update</div>
      </header>

      {error ? (
        <div className="card card-detail">
          <h2>Error</h2>
          <p className="detail-copy">{error}</p>
        </div>
      ) : (
        <>
          <section className="summary-grid">
            <article className="card card-highlight">
              <p className="card-label">Total Memory</p>
              <p className="card-value">{memory.total} GB</p>
            </article>
            <article className="card card-primary">
              <p className="card-label">Used Memory</p>
              <p className="card-value">{memory.used} GB</p>
            </article>
            <article className="card card-secondary">
              <p className="card-label">Available Memory</p>
              <p className="card-value">{memory.available} GB</p>
            </article>
            <article className="card card-usage">
              <p className="card-label">Current Usage</p>
              <p className="card-value">{memory.percent}%</p>
              <div className="progress-bar">
                <div className="progress-fill" style={{ width: `${memory.percent}%` }}></div>
              </div>
            </article>
          </section>

          <section className="detail-grid">
            <article className="card card-detail chart-card">
              <h2>Memory Trend</h2>
              <div className="chart-wrapper">
                <Line data={chartData} options={chartOptions} />
              </div>
              <p className="detail-copy">We track memory usage over time to visualize system pressure and history.</p>
            </article>
            <article className="card card-detail">
              <h2>Prediction</h2>
              <p className="detail-value">Predicted usage: <strong>{prediction}%</strong></p>
              <p className="detail-copy">The system uses current metrics to forecast memory pressure and help prevent overload.</p>
            </article>
          </section>

          <section className="detail-grid">
            <article className="card card-detail table-card">
              <h2>Recent Memory Logs</h2>
              <p className="detail-copy">Showing the last 5 records from the database for quick history tracking.</p>
              {loading ? (
                <p className="detail-copy">Loading recent logs...</p>
              ) : (
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Time</th>
                      <th>Used (GB)</th>
                      <th>Percent</th>
                      <th>Status</th>
                      <th>Email Sent</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentLogs.length === 0 ? (
                      <tr><td colSpan="5">No recent logs available.</td></tr>
                    ) : (
                      recentLogs.map((record) => (
                        <tr key={record.id}>
                          <td>{formatTimestamp(record.createdAt)}</td>
                          <td>{record.used}</td>
                          <td>{record.percent}%</td>
                          <td>{record.alertStatus}</td>
                          <td>{record.alertSent ? 'Yes' : 'No'}</td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              )}
            </article>
            <article className="card card-detail table-card">
              <h2>Alert History</h2>
              <p className="detail-copy">We track all critical memory events with alert status = HIGH.</p>
              {alertLogs.length === 0 ? (
                <p className="detail-copy">No high-memory alerts have been recorded yet.</p>
              ) : (
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Time</th>
                      <th>Used (GB)</th>
                      <th>Percent</th>
                      <th>Email Sent</th>
                    </tr>
                  </thead>
                  <tbody>
                    {alertLogs.map((record) => (
                      <tr key={record.id}>
                        <td>{formatTimestamp(record.createdAt)}</td>
                        <td>{record.used}</td>
                        <td>{record.percent}%</td>
                        <td>{record.alertSent ? 'Yes' : 'No'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </article>
          </section>

          <section className="detail-grid">
            <article className="card card-detail">
              <h2>Cache Status</h2>
              <p className="detail-value">{action}</p>
              <p className="detail-copy">Cache management action executed to optimize memory availability.</p>
            </article>
          </section>
        </>
      )}

      <footer className="app-footer">
        <p>Powered by local memory metrics, cache control, and predictive analytics.</p>
      </footer>
    </div>
  );
}

export default App;
