import React, { useState } from 'react';

const ProfileCreator = () => {
    const [currentSection, setCurrentSection] = useState(0);
    const [responses, setResponses] = useState({
        username: '',
        lookingFor: [],
        dealBreakers: [],
        interests: [],
        ageRange: '',
        locationPreference: ''
    });

    const [currentInput, setCurrentInput] = useState('');

    const handleNext = () => {
        switch(currentSection) {
            case 0: // Username
                if (currentInput) {
                    setResponses(prev => ({ ...prev, username: currentInput }));
                    setCurrentSection(1);
                    setCurrentInput('');
                }
                break;
            case 1: // Looking for
            case 2: // Deal breakers
            case 3: // Interests
                const key = ['lookingFor', 'dealBreakers', 'interests'][currentSection - 1];
                if ((responses[key]).length > 0) {
                    setCurrentSection(prev => prev + 1);
                    setCurrentInput('');
                }
                break;
            // ... handle other sections
        }
    };

    const addItem = () => {
        if (!currentInput.trim()) return;

        const key = ['lookingFor', 'dealBreakers', 'interests'][currentSection - 1];
        const cleanedInput = currentInput.trim().replace(/^•\s*/, '');

        setResponses(prev => ({
            ...prev,
            [key]: [...(prev[key]), cleanedInput]
        }));
        setCurrentInput('• ');
    };

    const debugPrint = () => {
        console.log(responses)
    }

    return (
        <div className="container">
            <div className="profile-card">
            <h1 className="header">HeartHack Profile</h1>
            
            <div className="question-frame">
                <label className="question-label">
                    {/* Question text based on currentSection */}
                </label>

                <textarea 
                    className="text-input"
                    value={currentInput}
                    onChange={(e) => setCurrentInput(e.target.value)}
                    onKeyPress={(e) => {
                        if (e.key === 'Enter') {
                            e.preventDefault();
                            if ([1,2,3].includes(currentSection)) {
                                addItem();
                            } else {
                                handleNext();
                            }
                        }
                    }}
                />

                <ul className="items-list">
                    {[1,2,3].includes(currentSection) && 
                        (responses[['lookingFor', 'dealBreakers', 'interests'][currentSection - 1]])
                        .map((item, index) => (
                            <li key={index}>{item}</li>
                        ))
                    }
                </ul>

                <div className="button-frame">
                    {[1,2,3].includes(currentSection) && (
                        <button className="button" onClick={addItem}>
                            Add Item
                        </button>
                    )}
                   <button 
                        className="button" 
                        onClick={handleNext}
                    >
                        {currentSection === 5 ? 'Finish' : 'Next'}
                    </button>
                    {/* <button className="button" onClick={debugPrint}>Debug print state</button> */}
                </div>
            </div>
           </div>
        </div>
    );
};

export default ProfileCreator; 