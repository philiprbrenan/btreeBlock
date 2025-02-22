*******************************************************************************
** © Copyright 2014 Xilinx, Inc. All rights reserved.
** This file contains confidential and proprietary information of Xilinx, Inc. and 
** is protected under U.S. and international copyright and other intellectual property laws.
*******************************************************************************
**   ____  ____ 
**  /   /\/   / 
** /___/  \  /   Vendor: Xilinx 
** \   \   \/    
**  \   \        readme.txt Version: 1.12
**  /   /        Date Last Modified:2/28/14
** /___/   /\    Date Created: 04/29/11
** \   \  /  \   Associated Filename: 7vSeriesAll.zip
**  \___\/\___\ 
** 
**  Device: 7 Series FPGAs
**  Revision History: 
**	v1.0:	Added XC7VX485T packages
**	v1.1:	Added XC7V2000T packages
**	v1.2:	Updated FFG1157 package and added FFG1930 package for XC7VX485T
**	v1.3:	Changed XC7V330T to XC7VX330T in XC7V2000T FFG1761 package No Connect column
**	v1.4:	Added XC7V585T packages
**	v1.5:	Changed value of VCCAUX Group to NA for MGT pins in all files
**	     	Moved AD4P/N, AD12P/N, and AD5P/N pins from [IO_L2P_T0_35:IO_L4N_T0_35] to [IO_L1P_T0_35:IO_L3N_T0_35] for all packages
**	     	Changed TDO_x to TDO_0 and TDO bank number to 0 for XC7V2000T packages
**	v1.6:	Added FLG1761 package for XC7V1500T
**		Added FFG1157 package for XC7VX330T, XC7VX415T, and XC7VX690T
**		Added FFG1158 package for XC7VX415T, XC7VX550T, and XC7VX690T
**		Added FFG1761 package for XC7VX330T and XC7VX690T
**		Added FFG1926 package for XC7VX690T and XC7VX980T
**		Added FFG1927 package for XC7VX415T, XC7VX550T, and XC7VX690T
**		Added FFG1928 package for XC7VX980T
**		Added FFG1930 package for XC7VX690T
**		Added FLG1928 package for XC7VX1140T
**		Added FLG1930 package for XC7VX1140T
**	v1.7:	Added FLG1926 package for XC7VX1140T
**		Removed FFG1933 and FFG1933 packages
**	v1.8:	Changed XC7VX690T FFG1927 package No Connect column
**		Added VCCAUX_IO_G0 pins to XC7V585T FFG1761 package
**		Added XC7VH290T, XC7VH580T, and XC7VH870T packages
**	v1.9:	Removed XC7V1500T packages
**		Removed XC7VH290T packages
**		Changed No-Connect column in XC7VH580T HCG1155 package
**		Changed bank number for TDO_0 from 3 to 0 in XC7VX1400T packages
**	v1.10:	Changed IO Type and No-Connect columns in XC7VX550T FFG1927 package
**		Changed Super Logic Region numbering in XC7VH870T HCG1932 package
**	v1.11:	Removed XC7VH580T-HCG1932 package
**		Removed XC7VH870T-HCG1931 package
**		Changed file name for 7VH580T-HCG1155 to support FLG1155 package
**		Changed file name for 7VH580T-HCG1931 to support FLG1931 package
**		Changed file name for 7VH870T-HCG1932 to support FLG1932 package
**	v1.12:	Changed bank number and I/O type for TDO_0 pin in 7VH580T and 7VH870T packages
**		Changed Super Logic Region numbering in XC7VH870T FLG1932/HCG1932 package
**
*******************************************************************************
**
**  Disclaimer: 
**
**	This disclaimer is not a license and does not grant any rights to the materials 
**             	distributed herewith. Except as otherwise provided in a valid license issued to you 
**	by Xilinx, and to the maximum extent permitted by applicable law: 
**	(1) THESE MATERIALS ARE MADE AVAILABLE "AS IS" AND WITH ALL FAULTS, 
**	AND XILINX HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EXPRESS, IMPLIED, OR STATUTORY, 
**	INCLUDING BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT, OR 
**	FITNESS FOR ANY PARTICULAR PURPOSE; and (2) Xilinx shall not be liable (whether in contract 
**	or tort, including negligence, or under any other theory of liability) for any loss or damage 
**	of any kind or nature related to, arising under or in connection with these materials, 
**	including for any direct, or any indirect, special, incidental, or consequential loss 
**	or damage (including loss of data, profits, goodwill, or any type of loss or damage suffered 
**	as a result of any action brought by a third party) even if such damage or loss was 
**	reasonably foreseeable or Xilinx had been advised of the possibility of the same.


**  Critical Applications:
**
**	Xilinx products are not designed or intended to be fail-safe, or for use in any application 
**	requiring fail-safe performance, such as life-support or safety devices or systems, 
**	Class III medical devices, nuclear facilities, applications related to the deployment of airbags,
**	or any other applications that could lead to death, personal injury, or severe property or 
**	environmental damage (individually and collectively, "Critical Applications"). Customer assumes 
**	the sole risk and liability of any use of Xilinx products in Critical Applications, subject only 
**	to applicable laws and regulations governing limitations on product liability.

**  THIS COPYRIGHT NOTICE AND DISCLAIMER MUST BE RETAINED AS PART OF THIS FILE AT ALL TIMES.

*******************************************************************************
** IMPORTANT NOTES **

1) These ASCII text files contain advance information and are subject to change without notice and are provided solely for information purposes.

2) Please refer to the 7 Series FPGAs Package Files section of UG475, 7 Series FPGAs Packaging and Pinout, for detailed information on the contents of the ASCII text files.

 
