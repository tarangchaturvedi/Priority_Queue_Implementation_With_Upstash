# Queue Service Implementation

### Table of Contents
1. [Introduction](#introduction)
2. [Features](#features)
3. [Installation](#installation)
4. [Usage](#usage)
5. [Technologies Used](#technologies-used)
6. [Project Structure](#project-structure)
7. [Test Cases](#test-cases)
8. [Contributing](#contributing)
9. [License](#license)
10. [Contact](#contact)

---

## Introduction

This project is a Queue Service enhancement assignment focusing on:
1. Implementing a priority queue in an existing codebase.
2. Developing a new queue using Upstash as the backend.

The project includes priority-based request handling, adherence to First-Come-First-Serve (FCFS) within the same priority level, and comprehensive test cases for validation.

---

## Features

- **Priority Queue System**: Requests are assigned numerical priorities for efficient retrieval.
- **InMemoryPriorityQueue**: Enhanced to support priority-based polling and FCFS order.
- **Upstash-Based Queue**: Uses Kafka or Redis via Upstash HTTP API, with the same priority system.
- **Test Cases**: Comprehensive tests for functionality and edge cases.

---

## Installation

### Prerequisites
1. Python 3.8 or higher
2. [Upstash account](https://upstash.com/)
3. Required Python libraries (see `requirements.txt`)

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/queue-service.git
   ```
2. Navigate to the project directory:
   ```bash
   cd queue-service
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

---

## Usage

### Running the InMemoryPriorityQueue
1. Run the script to start the InMemoryPriorityQueue:
   ```bash
   python in_memory_priority_queue.py
   ```
2. Add and poll requests with priority using the CLI or API endpoints.

### Running the Upstash-Based Queue
1. Configure your Upstash API credentials in the `.env` file.
2. Start the Upstash queue implementation:
   ```bash
   python upstash_queue.py
   ```
3. Interact with the queue through the provided API or test scripts.

---

## Technologies Used

- Python
- Redis/Kafka (via Upstash)
- HTTP APIs for Upstash integration
- Testing: `unittest` or `pytest`

---

## Project Structure

```plaintext
├── src/
│   ├── in_memory_priority_queue.py   # Implementation of InMemoryPriorityQueue
│   ├── upstash_queue.py              # Upstash-based queue implementation
├── tests/
│   ├── test_in_memory.py             # Tests for InMemoryPriorityQueue
│   ├── test_upstash.py               # Tests for Upstash queue
├── requirements.txt                  # Python dependencies
├── README.md                         # Project documentation
└── .env                              # Environment variables (for Upstash API keys)
```

---

## Test Cases

### InMemoryPriorityQueue
- Validates priority-based polling.
- Ensures FCFS order within the same priority.
- Covers edge cases such as empty queues and duplicate priorities.

### Upstash-Based Queue
- Tests priority-based ordering via Upstash.
- Validates FCFS behavior for requests with identical priorities.
- Handles API errors and retries.

Run tests using:
```bash
pytest tests/
```

---

## Contributing

We welcome contributions! Please follow these steps to contribute:
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature-branch-name`).
3. Commit your changes (`git commit -m "Add feature"`).
4. Push the branch to your fork (`git push origin feature-branch-name`).
5. Open a pull request.

---

## License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

---

## Contact

For questions, suggestions, or collaborations, please reach out:
- Name: [Your Name]
- Email: [your.email@example.com]
- GitHub: [https://github.com/your-username](https://github.com/your-username)
